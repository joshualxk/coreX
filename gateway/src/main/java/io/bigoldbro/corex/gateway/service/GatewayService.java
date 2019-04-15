package io.bigoldbro.corex.gateway.service;

import io.bigoldbro.corex.*;
import io.bigoldbro.corex.annotation.Value;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;
import io.bigoldbro.corex.exception.BizEx;
import io.bigoldbro.corex.exception.BizException;
import io.bigoldbro.corex.gateway.handler.GatewayBridgeHandler;
import io.bigoldbro.corex.gateway.handler.GatewayHttpHandler;
import io.bigoldbro.corex.impl.SessionManagerImpl;
import io.bigoldbro.corex.module.GatewayModule;
import io.bigoldbro.corex.proto.Base;
import io.bigoldbro.corex.service.SimpleModuleService;
import io.bigoldbro.corex.utils.CoreXUtil;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.internal.StringUtil;

import java.util.Map;

/**
 * Created by Joshua on 2018/2/26.
 */
public class GatewayService extends SimpleModuleService implements GatewayModule {

    private final SessionManager sessionManager = new SessionManagerImpl();

    @Value("corex.gateway.port")
    private int port;

    @Override
    public void init(Context context) {
        super.init(context);
    }

    @Override
    public void start(Future<Void> completeFuture) {
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

            if (msg instanceof Base.ClientPayload) {
                Base.ClientPayload clientPayload = (Base.ClientPayload) msg;

                if (clientPayload.hasRequest()) {
                    Base.ClientRequest clientRequest = clientPayload.getRequest();


                    Base.Request request = Base.Request.newBuilder()
                            .setId(clientRequest.getId())
                            .setMethod(clientRequest.getMethod())
                            .setAuth(getAuth(conn))
                            .setTimestamp(clientRequest.getTimestamp())
                            .build();

                    moduleRouter(conn, request);
                }
            }
        });

        conn.closeHandler(v -> {
            sessionManager.leave(conn);
            logger.debug("on conn close:{}", conn);
        });
    }

    private Base.Auth getAuth(Connection conn) {
        String userId = sessionManager.userId(conn);
        if (userId == null) {
            return Base.Auth.newBuilder()
                    .setType(ConstDefine.AUTH_TYPE_NON)
                    .build();
        }

        return Base.Auth.newBuilder()
                .setType(ConstDefine.AUTH_TYPE_CLIENT)
                .setToken(userId)
                .build();
    }

    private void moduleRouter(Connection conn, Base.Request request) {
        String module = request.getMethod().getModule();
        String api = request.getMethod().getApi();
        if (ServiceNameDefine.SPECIAL.equals(module)) {
            handleLocalRequest(conn, request);
        } else if (!ServiceNameDefine.isValidName(module) || ServiceNameDefine.isInternalName(module) || StringUtil.isNullOrEmpty(api)) {
            // do nothing
        } else if ("connect".equals(api)) { // connect 接口不需要登录
            sendRequestForward(request, conn);
        } else {
            if (!sessionManager.hasLogin(conn)) {
                sendResponse(conn, request.getId(), ExceptionDefine.NOT_LOGIN);
                return;
            }

            sendRequestForward(request, conn);
        }
    }

    private void handleLocalRequest(Connection conn, Base.Request request) {
        try {
            String api = request.getMethod().getApi();
            if ("login".equals(api)) {
                handleLogin(conn, request);
            } else if ("logout".equals(api)) {
                handleLogout(conn, request);
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

    private static Handler<AsyncResult<Base.Payload>> resultHandler(Connection conn, int requestId) {

        return new OnlySuccessHandler<Base.Payload>(conn, requestId) {
            @Override
            public void onSuccess(Base.Payload o) {
                if (o.hasResponse()) {
                    sendResponse(conn, o.getResponse());
                }
            }
        };
    }

    private static void sendResponse(Connection conn, Base.Response rpcResponse) {
        conn.write(rpcResponse);
    }

    private static void sendResponse(Connection conn, int requestId, BizEx bizEx) {
        conn.write(CoreXUtil.failedResponse(requestId, bizEx));
    }

    private void handleLogin(Connection conn, Base.Request request) {
        if (sessionManager.hasLogin(conn)) {
            sendResponse(conn, request.getId(), ExceptionDefine.ALREADY_LOGIN);
            return;
        }

        sendRequestForward(request, conn);
    }

    private void handleLogout(Connection conn, Base.Request request) {
        if (!sessionManager.hasLogin(conn)) {
            sendResponse(conn, request.getId(), ExceptionDefine.NOT_LOGIN);
            return;
        }

        String userId = sessionManager.logout(conn);
        // TODO notify login server

        sendResponse(conn, CoreXUtil.successResponse(request.getId()));

        logger.info("[session] userId:{}, action:logout.", userId);
    }

    private void handleTest(Connection conn, Base.Request request) {
        sendResponse(conn, CoreXUtil.successResponse(request.getId()));
    }

    private void sendRequestForward(Base.Request request, Connection conn) {
        int requestId = request.getId();
        Handler<AsyncResult<Base.Payload>> handler = resultHandler(conn, requestId);
        coreX().sendMessage(request.getMethod().getModule(), request, handler);
    }

    @Override
    public Map<String, String> info() {
        Map<String, String> ret = super.info();
        ret.put("port", String.valueOf(port));
        return ret;
    }

    private static abstract class OnlySuccessHandler<T> implements Handler<AsyncResult<T>> {

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
