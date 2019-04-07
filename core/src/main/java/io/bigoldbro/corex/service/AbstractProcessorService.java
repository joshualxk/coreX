package io.bigoldbro.corex.service;

import io.bigoldbro.corex.*;
import io.bigoldbro.corex.annotation.BlockControl;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.exception.BizException;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.impl.SucceededCallback;
import io.bigoldbro.corex.json.Json;
import io.bigoldbro.corex.json.JsonObject;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.model.*;
import io.bigoldbro.corex.rpc.RpcHandler;
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

            Callback<Object> ret = rpcHandler.handle(auth, request.getBody());

            if (ret == null) {
                if (msg.needReply()) {
                    throw new CoreException("方法返回值不能为空, method:" + request.getMethod());
                }
            } else if (ret instanceof SucceededCallback) {
                Object body = ret.sync();
                RpcResponse rpcResponse = RpcResponse.newSuccessRpcResponse(request.getId(), Json.wrap(body));
                Payload b = Payload.newPayload(payload.getId(), rpcResponse);
                ar = Future.succeededFuture(b);

            } else {
                int requestId = request.getId();
                long payloadId = payload.getId();

                if (msg.needReply()) {
                    ret.setHandler(ar2 -> {
                        if (ar2.succeeded()) {
                            Object body = ar2.result();
                            RpcResponse rpcResponse = RpcResponse.newSuccessRpcResponse(requestId, Json.wrap(body));
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
    public Callback<JsonObject> info() {
        JsonObject jo = new JsonObjectImpl();
        jo.put("clz", getClass().getName());
        jo.put("addr", name());
        jo.put("bc", bc().toString());
        return new SucceededCallback<>(jo);
    }

    private static String rpcHandlerName(RpcHandler rpcHandler) {
        String name;
        return (rpcHandler == null || (name = rpcHandler.name()) == null) ? "未知rpcHandler" : name;
    }

    protected abstract RpcHandler getHandler(String name);

    protected abstract Module getModule();

}
