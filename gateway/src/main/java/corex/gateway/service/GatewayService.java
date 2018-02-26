package corex.gateway.service;

import corex.core.*;
import corex.core.define.ExceptionDefine;
import corex.core.define.ServiceNameDefine;
import corex.core.exception.BizEx;
import corex.core.exception.BizException;
import corex.core.impl.ReadOnlyFutureMo;
import corex.core.impl.SessionManagerImpl;
import corex.core.impl.handler.BridgeHandler;
import corex.core.service.SimpleModuleService;
import corex.core.utils.CoreXUtil;
import corex.gateway.handler.GatewayCodecHandler;
import corex.module.GatewayModule;
import corex.module.LoginModule;
import corex.proto.ModelProto.ClientPayload;
import corex.proto.ModelProto.Payload;
import corex.proto.ModelProto.RpcRequest;
import corex.proto.ModelProto.RpcResponse;
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
    private String webroot;

    @Override
    public void init(Context context) {
        super.init(context);
    }

    @Override
    public void start(Future<Void> completeFuture) {
        port = coreX().config().getHttpPort();
        webroot = coreX().config().getWebRoot();

        sessionManager.init(context);

        BridgeHandler bridgeHandler = new BridgeHandler(context(), this::handleConn);

        coreX().createNetServer(port, new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast("httpServerCodec", new HttpServerCodec());
                pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(65536));
                pipeline.addLast("gatewayCodecHandler", new GatewayCodecHandler(coreX().codec(), false, webroot));
                pipeline.addLast("bridgeHandler", bridgeHandler);
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
                        logger.info("消息格式不正确:{}.", request);
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

            request = CoreXUtil.authorize(request, sessionManager.userId(conn));
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

    private Handler<AsyncResult<Object>> resultHandler(Connection conn, int requestId) {

        return new OnlySuccessHandler<Object>(conn, requestId) {
            @Override
            public void onSuccess(Object o) {
                if (o instanceof Payload) {
                    Payload payload = (Payload) o;

                    if (payload.hasRpcResponse()) {
                        sendResponse(conn, payload.getRpcResponse());
                        return;
                    }
                }
                logger.warn("返回类型错误:{}.", o);
                sendResponse(conn, requestId, ExceptionDefine.SYSTEM_ERR);
            }
        };
    }

    private static void sendResponse(Connection conn, RpcResponse rpcResponse) {
        conn.write(rpcResponse);
    }

    private static void sendResponse(Connection conn, int requestId, BizEx bizEx) {
        conn.write(CoreXUtil.bizExRpcResponse(requestId, bizEx));
    }

    private void handleLogin(Connection conn, RpcRequest request) {
        if (sessionManager.hasLogin(conn)) {
            sendResponse(conn, request.getId(), ExceptionDefine.ALREADY_LOGIN);
            return;
        }

        String token = request.getAuth().getToken();
        if (StringUtil.isNullOrEmpty(token)) {
            sendResponse(conn, request.getId(), ExceptionDefine.PARAM_ERR);
            return;
        }

        coreX().asyncAgent(LoginModule.class).authorize(token)
                .addListener(new OnlySuccessHandler<Mo>(conn, request.getId()) {
                    @Override
                    public void onSuccess(Mo futureMapObject) {
                        String userId = futureMapObject.getString("userId");

                        if (sessionManager.hasLogin(conn)) {
                            sendResponse(conn, request.getId(), ExceptionDefine.ALREADY_LOGIN);
                            return;
                        }

                        sessionManager.login(conn, userId);

                        FutureMo param = FutureMo.futureMo();
                        param.putString("userId", userId);
                        sendResponse(conn, CoreXUtil.newRpcResponse(request.getId(), param.toBodyHolder()));

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

        sendResponse(conn, CoreXUtil.newRpcResponse(request.getId()));

        logger.info("[session] userId:{}, action:logout.", userId);
    }

    private void handleRegister(Connection conn, RpcRequest request) throws Exception {
        Mo params = new ReadOnlyFutureMo(request.getBody());
        String channel = params.getString("register");
        if (StringUtil.isNullOrEmpty(channel) || channel.length() > 50) {
            throw new IllegalArgumentException("channel长度不合法");
        }

        sessionManager.register(conn, channel);
        sendResponse(conn, CoreXUtil.newRpcResponse(request.getId()));
    }

    private void handleUnregister(Connection conn, RpcRequest request) {
        sessionManager.unregister(conn);
        sendResponse(conn, CoreXUtil.newRpcResponse(request.getId()));
    }

    private void handleTest(Connection conn, RpcRequest request) {
        sendResponse(conn, CoreXUtil.newRpcResponse(request.getId()));
    }

    private void sendRequestForward(RpcRequest request, Connection conn, int requestId) {
        Handler<AsyncResult<Object>> handler = resultHandler(conn, requestId);
        coreX().sendMessage(request.getMethod().getModule(), request, handler);
    }

    @Override
    public FutureMo info() {
        FutureMo ret = baseInfo();
        ret.putInt("port", port);
        ret.putString("webroot", webroot);
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
