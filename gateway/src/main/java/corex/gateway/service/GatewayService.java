package corex.gateway.service;

import corex.core.*;
import corex.core.define.ExceptionDefine;
import corex.core.define.ServiceNameDefine;
import corex.core.exception.BizEx;
import corex.core.exception.BizException;
import corex.core.impl.SessionManagerImpl;
import corex.gateway.handler.GatewayBridgeHandler;
import corex.core.json.JsonObject;
import corex.core.model.ClientPayload;
import corex.core.model.Payload;
import corex.core.model.RpcRequest;
import corex.core.model.RpcResponse;
import corex.core.service.SimpleModuleService;
import corex.core.utils.CoreXUtil;
import corex.gateway.handler.GatewayHttpHandler;
import corex.module.GatewayModule;
import corex.module.LoginModule;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.internal.StringUtil;

/**
 * Created by Joshua on 2018/2/26.
 */
public class GatewayService extends SimpleModuleService implements GatewayModule {

    private final SessionManager sessionManager = new SessionManagerImpl();

    private int port;

    @Override
    public void init(Context context) {
        super.init(context);
    }

    @Override
    public void start(Future<Void> completeFuture) {
        port = coreX().config().getHttpPort();

        sessionManager.init(context);

        GatewayBridgeHandler gatewayBridgeHandler = new GatewayBridgeHandler(context(), this::handleConn);

        coreX().createNetServer(port, new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast("httpServerCodec", new HttpServerCodec());
                pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(65536));
                pipeline.addLast("gatewayCodecHandler", new GatewayHttpHandler(Codec.defaultCodec(), false));
                pipeline.addLast("gatewayBridgeHandler", gatewayBridgeHandler);
            }
        }, ar -> {
            if (ar.succeeded()) {
                completeFuture.complete(ar.result());
            } else {
                completeFuture.fail(ar.cause());
            }
        });
    }

    private void handleConn(Connection conn) {
        logger.debug("on conn:{}.", conn);

        conn.msgHandler(msg -> {
            logger.debug("on conn msg.");

            if (msg instanceof ClientPayload) {
                ClientPayload clientPayload = (ClientPayload) msg;

                if (clientPayload.hasRpcRequest()) {
                    RpcRequest request = clientPayload.getRpcRequest();

                    if (CoreXUtil.validateRpcRequest(request)) {
                        moduleRouter(conn, request);
                    } else {
                        logger.debug("客户端验证类型不正确.");
                    }
                }
            }
        });

        conn.closeHandler(v -> {
            sessionManager.leave(conn);
            logger.debug("on conn close:{}", conn);
        });
    }

    private void moduleRouter(Connection conn, RpcRequest request) {
        String module = request.getMethod().getModule();
        String api = request.getMethod().getApi();
        if (ServiceNameDefine.SPECIAL.equals(module)) {
            handleLocalRequest(conn, request);
        } else if (!ServiceNameDefine.isValidName(module) || ServiceNameDefine.isInternalName(module) || StringUtil.isNullOrEmpty(api)) {
            // do nothing
        } else if ("connect".equals(api)) { // connect 接口不需要登录
            sendRequestForward(request, conn, request.getId());
        } else {
            if (!sessionManager.hasLogin(conn)) {
                sendResponse(conn, request.getId(), ExceptionDefine.NOT_LOGIN);
                return;
            }

            request = request.authorize(sessionManager.userId(conn));
            sendRequestForward(request, conn, request.getId());
        }
    }

    private void handleLocalRequest(Connection conn, RpcRequest request) {
        try {
            String api = request.getMethod().getApi();
            if ("login".equals(api)) {
                handleLogin(conn, request);
            } else if ("logout".equals(api)) {
                handleLogout(conn, request);
            } else if ("register".equals(api)) {
                handleRegister(conn, request);
            } else if ("unregister".equals(api)) {
                handleUnregister(conn, request);
            } else if ("test".equals(api)) {
                handleTest(conn, request);
            } else {
                sendResponse(conn, request.getId(), ExceptionDefine.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            sendResponse(conn, request.getId(), ExceptionDefine.PARAM_ERR);
        } catch (BizException e) {
            sendResponse(conn, request.getId(), e);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(conn, request.getId(), ExceptionDefine.SYSTEM_ERR);
        }
    }

    private Handler<AsyncResult<Payload>> resultHandler(Connection conn, int requestId) {

        return new OnlySuccessHandler<Payload>(conn, requestId) {
            @Override
            public void onSuccess(Payload o) {
                if (o.hasRpcResponse()) {
                    sendResponse(conn, o.getRpcResponse());
                }
            }
        };
    }

    private static void sendResponse(Connection conn, RpcResponse rpcResponse) {
        conn.write(rpcResponse);
    }

    private static void sendResponse(Connection conn, int requestId, BizEx bizEx) {
        conn.write(RpcResponse.newBizExRpcResponse(requestId, bizEx));
    }

    private void handleLogin(Connection conn, RpcRequest request) {
        if (sessionManager.hasLogin(conn)) {
            sendResponse(conn, request.getId(), ExceptionDefine.ALREADY_LOGIN);
            return;
        }

        String token = request.getBody().getStringParam("to");

        coreX().asyncAgent(LoginModule.class).authorize(token)
                .addListener(new OnlySuccessHandler<JoHolder>(conn, request.getId()) {
                    @Override
                    public void onSuccess(JoHolder joHolder) {
                        String userId = joHolder.jo().getString("userId");

                        if (sessionManager.hasLogin(conn)) {
                            sendResponse(conn, request.getId(), ExceptionDefine.ALREADY_LOGIN);
                            return;
                        }

                        sessionManager.login(conn, userId);

                        JsonObject param = new JsonObject();
                        param.put("userId", userId);
                        sendResponse(conn, RpcResponse.newSuccessRpcResponse(request.getId(), param));

                        logger.info("[session] userId:{}, action:login.", userId);
                    }
                });
    }

    private void handleLogout(Connection conn, RpcRequest request) {
        if (!sessionManager.hasLogin(conn)) {
            sendResponse(conn, request.getId(), ExceptionDefine.NOT_LOGIN);
            return;
        }

        String userId = sessionManager.logout(conn);

        sendResponse(conn, RpcResponse.newSuccessRpcResponse(request.getId()));

        logger.info("[session] userId:{}, action:logout.", userId);
    }

    private void handleRegister(Connection conn, RpcRequest request) throws Exception {
        JsonObject jo = request.getBody();
        String channel = jo.getString("register");
        if (StringUtil.isNullOrEmpty(channel) || channel.length() > 50) {
            throw new IllegalArgumentException("channel长度不合法");
        }

        sessionManager.register(conn, channel);
        sendResponse(conn, RpcResponse.newSuccessRpcResponse(request.getId()));
    }

    private void handleUnregister(Connection conn, RpcRequest request) {
        sessionManager.unregister(conn);
        sendResponse(conn, RpcResponse.newSuccessRpcResponse(request.getId()));
    }

    private void handleTest(Connection conn, RpcRequest request) {
        sendResponse(conn, RpcResponse.newSuccessRpcResponse(request.getId()));
    }

    private void sendRequestForward(RpcRequest request, Connection conn, int requestId) {
        Handler<AsyncResult<Payload>> handler = resultHandler(conn, requestId);
        coreX().sendMessage(request.getMethod().getModule(), request, handler);
    }

    @Override
    public JoHolder info() {
        JoHolder ret = super.info();
        JsonObject jo = ret.jo();
        jo.put("port", port);
        return ret;
    }

    private abstract class OnlySuccessHandler<T> implements Handler<AsyncResult<T>> {

        private final Connection conn;
        private final int requestId;

        public OnlySuccessHandler(Connection conn, int requestId) {
            this.conn = conn;
            this.requestId = requestId;
        }

        public abstract void onSuccess(T t);

        @Override
        public void handle(AsyncResult<T> ar) {
            if (ar.succeeded()) {
                try {
                    onSuccess(ar.result());
                } catch (BizException e) {
                    sendResponse(conn, requestId, e);
                } catch (Exception e) {
                    logger.warn("error.", e);
                    sendResponse(conn, requestId, ExceptionDefine.SYSTEM_ERR);
                }
            } else {
                Throwable th = ar.cause();
                BizEx bizEx;
                if (th instanceof BizException) {
                    bizEx = (BizException) th;
                } else {
                    th.printStackTrace();
                    bizEx = ExceptionDefine.SYSTEM_ERR;
                }
                sendResponse(conn, requestId, bizEx);
            }
        }
    }
}
