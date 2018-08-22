package corex.core.service;

import corex.core.*;
import corex.core.define.ConstDefine;
import corex.core.define.ExceptionDefine;
import corex.core.exception.BizEx;
import corex.core.exception.BizException;
import corex.core.impl.RecoverableConnectionManager;
import corex.core.model.Broadcast;
import corex.core.model.Payload;
import corex.core.model.RpcRequest;
import corex.core.model.RpcResponse;
import corex.core.utils.CoreXUtil;
import corex.module.HarborServerModule;

/**
 * Created by Joshua on 2018/2/27.
 */
public class HarborServerService extends SimpleModuleService implements HarborServerModule {

    private RecoverableConnectionManager recoverableConnectionManager;

    private int port;

    @Override
    public void start(Future<Void> completeFuture) {
        port = coreX().config().getHarborPort();

        recoverableConnectionManager = new RecoverableConnectionManager(context, false);
        recoverableConnectionManager.openHandler(this::handleConn);
        recoverableConnectionManager.bind(port, completeFuture);
    }

    @Override
    protected void handleBroadcast(Msg msg, Payload payload, Broadcast broadcast) {
        handleBroadcast(payload, broadcast, null);
    }

    private void handleConn(Connection conn) {
        logger.debug("on conn:{}.", conn);

        conn.msgHandler(msg -> {
            logger.debug("on conn msg.");

            if (msg instanceof Payload) {
                Payload payload = (Payload) msg;

                if (payload.hasRpcRequest()) {
                    RpcRequest rpcRequest = payload.getRpcRequest();

                    String address = rpcRequest.getMethod().getModule();
                    if (CoreXUtil.needReply(payload.getId())) {
                        coreX().sendMessage(address, payload, resultHandler(payload.getId(), rpcRequest.getId(), conn));
                    } else {
                        coreX().sendMessage(address, payload, null);
                    }

                } else if (payload.hasBroadcast()) {
                    Broadcast broadcast = payload.getBroadcast();
                    handleBroadcast(payload, broadcast, conn);
                }
            }

        });

        conn.errorHandler(v -> {
            logger.debug("on conn error:{}.", conn);
        });

        conn.recoverHandler(v -> {
            logger.debug("on conn recover:{}.", conn);
        });

        conn.closeHandler(v -> {
            logger.debug("on conn close:{}.", conn);

            //TODO broadcast?
        });
    }

    private Handler<AsyncResult<Payload>> resultHandler(long id, int requestId, Connection conn) {
        return ar -> {
            Payload payload;

            if (ar.succeeded()) {
                payload = ar.result();
            } else {
                Throwable th = ar.cause();
                BizEx bizEx;
                if (th instanceof BizException) {
                    bizEx = (BizException) th;
                } else {
                    th.printStackTrace();
                    bizEx = ExceptionDefine.SYSTEM_ERR;
                }
                RpcResponse rpcResponse = RpcResponse.newBizExRpcResponse(requestId, bizEx);

                payload = Payload.newPayload(id, rpcResponse);
            }

            conn.write(payload);

        };

    }

    private void handleBroadcast(Payload payload, Broadcast broadcast, Connection excepted) {
        coreX().onBroadcast(broadcast);
        if (broadcast.getRole() == ConstDefine.ROLE_LOCAL) {
            return;
        }
        recoverableConnectionManager.broadcast(payload, broadcast.getRole(), excepted);
    }

}
