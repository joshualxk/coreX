package corex.core.impl;

import corex.core.AsyncResult;
import corex.core.Connection;
import corex.core.Context;
import corex.core.Handler;
import corex.core.exception.CoreException;
import corex.core.impl.handler.InitialHandler;
import corex.core.json.JsonObject;
import corex.core.model.Payload;
import corex.core.utils.CoreXUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Joshua on 2018/3/21.
 */
public class RecoverableConnectionManager {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 重连延时
    private static final long[] retryDelayTimes = {0, 500, 1000, 3000, 3000, 5000, 10000, 10000, 30000, 30000, 60000};

    // 服务端10秒后关闭
    private static final long CLOSE_INTERVAL = 10000;

    private final Context context;
    private final Set<Integer> serverIds = new HashSet<>();
    private final Map<Integer, RecoverableConnection> allConnections = new HashMap<>(); // serverId -> connection
    private final boolean clientSide;

    private Handler<Connection> openHandler;

    public RecoverableConnectionManager(Context context, boolean clientSide) {
        this.context = context;
        this.clientSide = clientSide;
    }

    public boolean addConnection(ServerInfo serverInfo) {
        if (!clientSide) {
            throw new CoreException("服务端不能调用addConnection");
        }
        if (serverIds.contains(serverInfo.getServerId())) {
            return false;
        }
        serverIds.add(serverInfo.getServerId());

        RecoverableConnection connection =
                new RecoverableConnection(context, serverInfo.getServerId(), serverInfo.getRole(),
                        RecoverableConnectionManager::delay, conn -> doConnect(conn, serverInfo));
        allConnections.put(serverInfo.getServerId(), connection);
        connection.openHandler(openHandler);
        doConnect(connection, serverInfo);
        return true;
    }

    public boolean removeConnection(ServerInfo serverInfo) {
        serverIds.remove(serverInfo.getServerId());
        return close(allConnections.get(serverInfo.getServerId()));
    }

    public void openHandler(Handler<Connection> handler) {
        this.openHandler = handler;
    }

    public RecoverableConnection get(int serverId) {
        return allConnections.get(serverId);
    }

    private boolean close(RecoverableConnection old) {
        if (old == null) {
            return false;
        }
        RecoverableConnection conn = allConnections.get(old.serverId());
        if (conn == old) {
            allConnections.remove(old.serverId());
            conn.close();
            return true;
        }
        return false;
    }

    public void broadcast(Payload payload, int target, Connection excepted) {
        for (Map.Entry<Integer, RecoverableConnection> entry : allConnections.entrySet()) {
            if (entry.getValue() == excepted) {
                continue;
            }
            if (CoreXUtil.isRole(target, entry.getValue().role())) {
                try {
                    entry.getValue().write(payload);
                } catch (Exception ignore) {

                }
            }
        }
    }

    private void doConnect(RecoverableConnection connection, ServerInfo serverInfo) {
        context.coreX().connectNetServer(serverInfo.getHost(), serverInfo.getPort(), new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline p = socketChannel.pipeline();
                CoreXUtil.initPipeline(p);

                InitialHandler initialHandler = new InitialHandler(context.coreX().serverId(), context.coreX().role(), context.coreX().startTime());
                p.addLast("initialHandler", initialHandler);
                initialHandler.setServerAuthHandler(auth -> context.executeFromIO(v -> {
                    final long startTime = auth.startTime;

                    RecoverableConnection conn = connection;
                    if (conn.getStartTime() == 0) {
                        conn.setStartTime(startTime);
                        conn.updateChannel(p.channel());
                        conn.onOpen();
                    } else if (conn.isOpen()) {
                        // 正常连接
                        p.close();
                    } else {
                        // 对方已重启
                        if (conn.createTime() != startTime) {
                            conn.close();
                            conn.updateChannel(p.channel());
                            conn.onOpen();
                        } else {
                            conn.updateChannel(p.channel());
                            conn.onRecover();
                        }
                    }

                    initialHandler.setPayloadHandler(msg -> context.executeFromIO(v2 -> conn.onMsg(msg)));
                }));
            }
        }, ar -> {
            if (!ar.succeeded()) {
                logger.info("fail to connect {}", serverInfo);
                connection.triggerErrorEvent();
            }
        });
    }

    private static long delay(int errorTimes) {
        if (errorTimes >= retryDelayTimes.length) {
            return retryDelayTimes[retryDelayTimes.length - 1];
        }
        return retryDelayTimes[errorTimes];
    }

    public void bind(int port, Handler<AsyncResult<Void>> resultHandler) {
        if (clientSide) {
            throw new CoreException("客户端不能调用addConnection");
        }
        context.coreX().createNetServer(port, new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline p = socketChannel.pipeline();
                CoreXUtil.initPipeline(p);

                InitialHandler initialHandler = new InitialHandler(context.coreX().serverId(), context.coreX().role(), context.coreX().startTime());
                p.addLast("initialHandler", initialHandler);
                initialHandler.setServerAuthHandler(auth -> context.executeFromIO(v -> {
                    final int serverId = auth.id;
                    final int role = auth.role;
                    final long startTime = auth.startTime;
                    final Channel channel = p.channel();

                    boolean requireNew = false;
                    if (allConnections.containsKey(serverId)) {
                        RecoverableConnection conn = allConnections.get(serverId);
                        if (conn.isOpen()) {
                            // 当前连接正常，关闭channel
                            p.close();
                        } else {
                            // 对方已经重启
                            if (conn.getStartTime() != startTime) {
                                close(conn);
                                requireNew = true;
                            } else {
                                conn.updateChannel(channel);
                                conn.onRecover();
                            }
                        }
                    } else {
                        requireNew = true;
                    }

                    if (requireNew) {
                        RecoverableConnection conn =
                                new RecoverableConnection(context, serverId, role, i -> CLOSE_INTERVAL, old -> close(old));
                        conn.setStartTime(startTime);
                        conn.openHandler(openHandler);
                        allConnections.put(serverId, conn);
                        initialHandler.setPayloadHandler(msg -> context.executeFromIO(v2 -> conn.onMsg(msg)));

                        conn.updateChannel(channel);
                        conn.onOpen();
                    }
                }));
            }
        }, resultHandler);
    }

    public JsonObject info() {
        JsonObject ret = new JsonObject();
        for (Map.Entry<Integer, RecoverableConnection> entry : allConnections.entrySet()) {
            ret.put("" + entry.getKey(), entry.getValue().isOpen());
        }
        return ret;
    }
}
