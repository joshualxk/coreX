package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.*;
import io.bigoldbro.corex.annotation.BlockControl;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.json.JsonArrayImpl;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.model.Broadcast;
import io.bigoldbro.corex.model.Payload;
import io.bigoldbro.corex.model.RpcRequest;
import io.bigoldbro.corex.module.BroadcastModule;
import io.bigoldbro.corex.rpc.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static io.bigoldbro.corex.utils.CoreXUtil.isRole;

/**
 * Created by Joshua on 2018/2/26.
 */
public class CoreXImpl implements CoreX {

    private static final Logger log = LoggerFactory.getLogger(CoreXImpl.class);

    public static final int DEFAULT_ACCEPTOR_POOL_SIZE = 1;

    public static final int DEFAULT_EVENT_LOOP_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    public static final int DEFAULT_WORKER_POOL_SIZE = 8;

    public static final int DEFAULT_MAX_PENDING_MSG_NUM = 10000;

    public static final long DEFAULT_REQUEST_TIME_OUT = 60_000; // 60s

    public static final long DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME = 2L * 1000 * 1000000; // 2s

    public static final long DEFAULT_MAX_WORKER_EXECUTE_TIME = 60L * 1000 * 1000000; // 60s

    public static final long DEFAULT_BLOCKED_THREAD_CHECK_INTERVAL = 1000;

    public static final long DEFAULT_WARNING_EXCEPTION_TIME = 5L * 1000 * 1000000;

    private final Map<String, Service> serviceMap = new ConcurrentHashMap<>();
    private final DefaultMsgHandler msgHandler;
    private final List<Handler<Broadcast>> internalBroadcastHandlers = new Vector<>();
    private final List<Handler<Broadcast>> externalBroadcastHandlers = new Vector<>();
    private final ConcurrentMap<Long, InternalTimerHandler> timeouts = new ConcurrentHashMap<>();
    private final RpcClient rpcClient;
    private final AtomicLong timeoutCounter = new AtomicLong(0);
    private final AtomicLong msgCounter = new AtomicLong(1);
    private final EventLoopGroup acceptorEventLoopGroup;
    private final EventLoopGroup eventLoopGroup;
    private final ExecutorService workerPool;
    private final CoreXConfig coreXConfig;
    private final int serverId;
    private final int role;
    private final long startTime;

    public CoreXImpl(CoreXConfig coreXConfig) {

        try {
            rpcClient = new RpcClient(this, ConstDefine.DEFAULT_MODULE_PACKAGE);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CoreException("初始化rpcClient失败");
        }

        this.coreXConfig = Objects.requireNonNull(coreXConfig, "coreXConfig");
        this.serverId = coreXConfig.getId();
        this.role = coreXConfig.getRole();
        if (this.role == ConstDefine.ROLE_LOCAL) {
            throw new CoreException("role配置错误");
        }
        this.startTime = System.currentTimeMillis();

        msgHandler = new DefaultMsgHandler(DEFAULT_MAX_PENDING_MSG_NUM, DEFAULT_REQUEST_TIME_OUT);

        BlockedThreadChecker checker = new BlockedThreadChecker(this, DEFAULT_BLOCKED_THREAD_CHECK_INTERVAL, DEFAULT_WARNING_EXCEPTION_TIME);
        acceptorEventLoopGroup = new NioEventLoopGroup(DEFAULT_ACCEPTOR_POOL_SIZE,
                new CoreXThreadFactory("io.bigoldbro.corex-acceptor-", checker, false, DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME));
        eventLoopGroup = new NioEventLoopGroup(DEFAULT_EVENT_LOOP_POOL_SIZE,
                new CoreXThreadFactory("io.bigoldbro.corex-eventLoop-", checker, false, DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME));
        workerPool = Executors.newFixedThreadPool(DEFAULT_WORKER_POOL_SIZE,
                new CoreXThreadFactory("io.bigoldbro.corex-worker-", checker, true, DEFAULT_MAX_WORKER_EXECUTE_TIME));
    }

    @Override
    public int serverId() {
        return serverId;
    }

    @Override
    public long startTime() {
        return startTime;
    }

    @Override
    public int role() {
        return role;
    }

    @Override
    public CoreXConfig config() {
        return coreXConfig;
    }

    @Override
    public EventLoopGroup acceptorEventLoopGroup() {
        return acceptorEventLoopGroup;
    }

    @Override
    public EventLoopGroup eventLoopGroup() {
        return eventLoopGroup;
    }

    @Override
    public ExecutorService executorService() {
        return workerPool;
    }

    public static Context getContext() {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof CoreXThread) {
            return ((CoreXThread) currentThread).getContext();
        }
        return null;
    }

    private Context createContext(String name, BlockControl bc, Service service) {
        switch (bc) {
            case NON_BLOCK:
                return new EventLoopContext(this, name, service);
            case BLOCK:
                return new WorkerContext(this, name, service);
            case MULTI_THREADED:
                return new MultiThreadedWorkerContext(this, name, service);
        }
        throw new CoreException("不支持的阻塞类型");
    }

    @Override
    public void startService(Service service, Handler<AsyncResult<Void>> resultHandler) {
        ensureNoContext();
        String name = service.name();
        BlockControl bc = service.bc();

        if (serviceMap.containsKey(name)) {
            resultHandler.handle(Future.failedFuture(new CoreException("服务 " + name + " 已启动!")));
            return;
        }

        log.info("启动服务, name:{}, bc:{}.", name, bc.toString());

        Context context = createContext(name, bc, service);

        context.runOnContext(v -> {
            Future<Void> future = Future.future();
            future.setHandler(ar -> {
                if (ar.succeeded()) {
                    if (serviceMap.putIfAbsent(name, service) != null) {
                        resultHandler.handle(Future.failedFuture(new CoreException("服务 " + name + " 已启动!")));
                        return;
                    }
                    try {
                        service.afterStart();
                    } catch (Exception e) {
                        resultHandler.handle(Future.failedFuture(e));
                        return;
                    }
                }
                resultHandler.handle(ar);
            });

            try {
                service.init(context);
                service.start(future);
            } catch (Throwable t) {
                future.fail(t);
            }
        });
    }

    @Override
    public void stopService(String name, Handler<AsyncResult<Void>> resultHandler) {
        ensureNoContext();
        if (!serviceMap.containsKey(name)) {
            log.warn("服务 " + name + " 已关闭!");
            return;
        }

        Service service = serviceMap.get(name);
        service.context().runOnContext(v -> {
            Future<Void> future = Future.future();
            future.setHandler(ar -> {
                if (ar.succeeded()) {
                    serviceMap.remove(name);
                }
                resultHandler.handle(ar);
            });

            try {
                service.stop(future);
            } catch (Throwable t) {
                future.fail(t);
            }
        });
    }

    private Context ensureContext() {
        Context context = getContext();
        if (context == null) {
            throw new CoreException("context为空");
        }
        return context;
    }

    private Context ensureNoContext() {
        Context context = getContext();
        if (context != null) {
            throw new CoreException("context不为空");
        }
        return context;
    }

    private Msg wrapMsg(Context context, boolean needReply, Object raw) {
        long id = needReply ? msgCounter.getAndIncrement() : 0;
        Payload body;
        if (raw instanceof RpcRequest) {
            RpcRequest rpcRequest = (RpcRequest) raw;
            body = Payload.newPayload(id, rpcRequest).addRoute(context.name());
        } else if (raw instanceof Broadcast) {
            Broadcast broadcast = (Broadcast) raw;
            body = Payload.newPayload(id, broadcast).addRoute(context.name());
        } else if (raw instanceof Payload) {
            body = (Payload) raw;
        } else {
            throw new CoreException("不支持的消息类型:" + (raw == null ? null : raw.getClass().getName()));
        }

        return new InternalMsg(context, id, body);
    }

    @Override
    public void sendMessage(String address, Object msg, Handler<AsyncResult<Payload>> replyHandler) {
        Context context = ensureContext();
        Service service = getService(address);

        Objects.requireNonNull(msg);
        if (service == null) {
            if (replyHandler != null) {
                context.runOnContext(v -> replyHandler.handle(Future.failedFuture(ExceptionDefine.NOT_FOUND.build())));
            } else {
                throw ExceptionDefine.NOT_FOUND.build();
            }
        } else {
            Msg sendMsg = wrapMsg(context, replyHandler != null, msg);
            if (replyHandler != null) {
                try {
                    onMsgSent(sendMsg.id(), replyHandler);
                } catch (Exception e) {
                    replyHandler.handle(Future.failedFuture(e));
                    return;
                }
            }
            service.context().runOnContext(v -> {
                service.handleMsg(sendMsg);
            });
        }
    }

    @Override
    public void broadcastMessage(Broadcast broadcast) {
        sendMessage(ServiceNameDefine.BROADCAST, broadcast, null);
    }

    @Override
    public void createNetServer(int port, ChannelHandler channelHandler, Handler<AsyncResult<Void>> resultHandler) {
        Context context = ensureContext();

        ServerBootstrap b = new ServerBootstrap();
        b.group(acceptorEventLoopGroup(), context.isWorker() ? eventLoopGroup : context.eventLoop())
                .channel(NioServerSocketChannel.class)
                .childHandler(channelHandler);

        b.bind(port).addListener(future -> {
            if (resultHandler != null) {
                context.runOnContext(v -> {
                    if (future.isSuccess()) {
                        resultHandler.handle(Future.succeededFuture());
                    } else {
                        resultHandler.handle(Future.failedFuture(future.cause()));
                    }
                });
            }
        });
    }

    @Override
    public void connectNetServer(String host, int port, ChannelHandler channelHandler, Handler<AsyncResult<Channel>> resultHandler) {
        Context context = ensureContext();

        Bootstrap b = new Bootstrap();
        b.group(context.isWorker() ? eventLoopGroup : context.eventLoop())
                .channel(NioSocketChannel.class)
                .handler(channelHandler);

        b.connect(host, port).addListener(future -> {
            if (resultHandler != null) {
                context.runOnContext(v -> {
                    if (future.isSuccess()) {
                        resultHandler.handle(Future.succeededFuture(((ChannelFuture) future).channel()));
                    } else {
                        resultHandler.handle(Future.failedFuture(future.cause()));
                    }
                });
            }
        });
    }

    @Override
    public long setTimer(long delay, Handler<Long> handler) {
        return scheduleTimeout(ensureContext(), handler, delay, false);
    }

    @Override
    public long setPeriodic(long delay, Handler<Long> handler) {
        return scheduleTimeout(ensureContext(), handler, delay, true);
    }

    @Override
    public boolean cancelTimer(long id) {
        InternalTimerHandler handler = timeouts.remove(id);
        if (handler != null) {
            return handler.cancel();
        } else {
            return false;
        }
    }

    @Override
    public <T> T asyncAgent(Class<T> clz) {
        T t = rpcClient.apiAgent(clz);
        if (t == null) {
            throw new CoreException("找不到对应服务:" + clz.getName());
        }
        return t;
    }

    @Override
    public BroadcastModule broadcast() {
        return rpcClient.broadcastAgent();
    }

    @Override
    public int subscribeBroadcast(Handler<Broadcast> internal, Handler<Broadcast> external) {
        Context context = ensureContext();

        int added = 0;
        if (internal != null && internalBroadcastHandlers.add(e -> context.runOnContext(v -> internal.handle(e)))) {
            added++;
        }
        if (external != null && externalBroadcastHandlers.add(e -> context.runOnContext(v -> external.handle(e)))) {
            added++;
        }
        return added;
    }

    @Override
    public void onBroadcast(Broadcast broadcast) {
        List<Handler<Broadcast>> handlers = broadcast.isInternal() ? internalBroadcastHandlers : externalBroadcastHandlers;
        for (Handler<Broadcast> h : handlers) {
            h.handle(broadcast);
        }
    }

    private Service getService(String name) {
        Service s;
        if (ServiceNameDefine.BROADCAST.equals(name)) {
            if (isRole(role(), ConstDefine.ROLE_GATEWAY)) {
                s = serviceMap.get(ServiceNameDefine.HARBOR_SERVER);
            } else {
                s = serviceMap.get(ServiceNameDefine.HARBOR_CLIENT);
            }
        } else {
            s = serviceMap.get(name);
            if (s == null) {
                if (isRole(role(), ConstDefine.ROLE_GATEWAY)) {
                    s = serviceMap.get(ServiceNameDefine.HARBOR_CLIENT);
                }
            }
        }
        return s;
    }

    private long scheduleTimeout(Context context, Handler<Long> handler, long delay, boolean periodic) {
        if (delay < 1) {
            throw new IllegalArgumentException("Cannot schedule a timer with delay < 1 ms");
        }
        long timerId = timeoutCounter.getAndIncrement();
        InternalTimerHandler task = new InternalTimerHandler(timerId, handler, periodic, delay, context);
        timeouts.put(timerId, task);
        return timerId;
    }

    @Override
    public void onMsgSent(long id, Handler<AsyncResult<Payload>> handler) {
        Context context = ensureContext();
        msgHandler.onMsgSent(id, ar -> {
            context.runOnContext(v -> handler.handle(ar));
        });
    }

    @Override
    public void onMsgReply(long id, AsyncResult<Payload> resp) {
        msgHandler.onMsgReply(id, resp);
    }

    @Override
    public void removeExpireMsg() {
        msgHandler.removeExpireMsg();
    }

    public JoHolder info() {
        JoHolder ret = JoHolder.newSync();
        JsonObjectImpl jo = ret.jo();
        jo.put("serverId", serverId);
        jo.put("role", role);
        jo.put("startTime", startTime);

        JsonArrayImpl ja = new JsonArrayImpl();
        for (Map.Entry<String, Service> entry : serviceMap.entrySet()) {
            ja.add(entry.getKey());
        }
        jo.put("services", ja);
        jo.put("timeoutsNum", timeouts.size());
        jo.put("pendingMsgNum", msgHandler.pendingMsgNum());

        return ret;
    }

    private class InternalTimerHandler implements Handler<Void> {

        final long timerID;
        final Handler<Long> handler;
        final boolean periodic;
        final long delay;
        final Context context;
        final java.util.concurrent.Future<?> future;
        final AtomicBoolean cancelled;

        InternalTimerHandler(long timerID, Handler<Long> handler, boolean periodic, long delay, Context context) {
            this.timerID = timerID;
            this.handler = handler;
            this.periodic = periodic;
            this.delay = delay;
            this.context = context;
            this.cancelled = new AtomicBoolean();
            EventLoop el = context.eventLoop();
            Runnable toRun = () -> context.runOnContext(this);
            if (periodic) {
                future = el.scheduleAtFixedRate(toRun, delay, delay, TimeUnit.MILLISECONDS);
            } else {
                future = el.schedule(toRun, delay, TimeUnit.MILLISECONDS);
            }
        }

        boolean cancel() {
            if (cancelled.compareAndSet(false, true)) {
                future.cancel(false);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void handle(Void v) {
            if (!cancelled.get()) {
                try {
                    handler.handle(timerID);
                } finally {
                    if (!periodic) {
                        cleanupNonPeriodic();
                    }
                }
            }
        }

        private void cleanupNonPeriodic() {
            CoreXImpl.this.timeouts.remove(timerID);
        }

    }
}
