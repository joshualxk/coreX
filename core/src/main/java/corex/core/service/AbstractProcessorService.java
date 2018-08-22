package corex.core.service;

import corex.core.*;
import corex.core.annotation.Module;
import corex.core.define.ExceptionDefine;
import corex.core.exception.BizException;
import corex.core.exception.CoreException;
import corex.core.impl.AsyncJoHolder;
import corex.core.json.JsonObject;
import corex.core.model.*;
import corex.core.rpc.BlockControl;
import corex.core.rpc.RpcHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Joshua on 2018/2/26.
 */
public abstract class AbstractProcessorService implements Service {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Context context;

    @Override
    public Context context() {
        return context;
    }

    @Override
    public void init(Context context) {
        this.context = context;
    }

    @Override
    public void start(Future<Void> completeFuture) {
        completeFuture.complete();
    }

    @Override
    public void afterStart() {
    }

    @Override
    public void stop(Future<Void> completeFuture) {
        completeFuture.complete();
    }

    @Override
    public String name() {
        return getModule().address();
    }

    @Override
    public BlockControl bc() {
        return getModule().bc();
    }

    @Override
    public final void handleMsg(Msg msg) {
        Payload payload = msg.detach().addRoute(context.name());
        if (payload.hasRpcRequest()) {
            handleRequest(msg, payload, payload.getRpcRequest());
        } else if (payload.hasBroadcast()) {
            handleBroadcast(msg, payload, payload.getBroadcast());
        }
    }

    protected void handleBroadcast(Msg msg, Payload payload, Broadcast broadcast) {
        // do nothing
    }

    protected void handleRequest(Msg msg, Payload payload, RpcRequest request) {
        logger.debug("on rpc request, id:{}.", request.getId());

        RpcHandler rpcHandler = null;
        AsyncResult<Payload> ar = null;
        try {
            rpcHandler = getHandler(request.getMethod().getApi());

            if (rpcHandler == null) {
                throw ExceptionDefine.NOT_FOUND.build();
            }

            Auth auth = request.getAuth();

            JoHolder ret = rpcHandler.handle(auth, request.getBody());

            if (ret == null) {
                if (msg.needReply()) {
                    throw new CoreException("方法返回值不能为空, method:" + request.getMethod());
                }
            } else if (ret.isSync()) {
                RpcResponse rpcResponse = RpcResponse.newSuccessRpcResponse(request.getId(), ret.jo());
                Payload b = Payload.newPayload(payload.getId(), rpcResponse);
                ar = Future.succeededFuture(b);

            } else {
                AsyncJoHolder futureJo = (AsyncJoHolder) ret;
                int requestId = request.getId();
                long payloadId = payload.getId();

                if (msg.needReply()) {
                    futureJo.setHandler(ar2 -> {
                        if (ar2.succeeded()) {
                            JoHolder ret2 = ar2.result();
                            RpcResponse rpcResponse = RpcResponse.newSuccessRpcResponse(requestId, ret2.jo());
                            Payload b = Payload.newPayload(payloadId, rpcResponse);
                            msg.reply(Future.succeededFuture(b));
                        } else {
                            msg.reply(Future.failedFuture(ar2.cause()));
                        }
                    });
                }

                return;
            }

        } catch (BizException e) {
            ar = Future.failedFuture(e);
        } catch (Throwable e) {
            logger.warn("方法执行错误,handler:{}.", rpcHandlerName(rpcHandler), e);
            ar = Future.failedFuture(e);
        }

        if (msg.needReply()) {
            msg.reply(ar);
        }
    }

    @Override
    public JoHolder info() {
        JoHolder ret = JoHolder.newSync();
        JsonObject jo = ret.jo();
        jo.put("clz", getClass().getName());
        jo.put("addr", name());
        jo.put("bc", bc().toString());
        return ret;
    }

    private static String rpcHandlerName(RpcHandler rpcHandler) {
        String name;
        return (rpcHandler == null || (name = rpcHandler.name()) == null) ? "未知rpcHandler" : name;
    }

    protected abstract RpcHandler getHandler(String name);

    protected abstract Module getModule();

}
