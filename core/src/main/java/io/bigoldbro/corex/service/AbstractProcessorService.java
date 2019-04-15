package io.bigoldbro.corex.service;

import io.bigoldbro.corex.*;
import io.bigoldbro.corex.annotation.BlockControl;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.exception.BizException;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.impl.CoreXImpl;
import io.bigoldbro.corex.proto.Base;
import io.bigoldbro.corex.rpc.RpcHandler;
import io.bigoldbro.corex.utils.CoreXUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joshua on 2018/2/26.
 */
public abstract class AbstractProcessorService implements Service {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Context context;

    private volatile int state;

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
        Base.Payload payload = msg.detach().toBuilder().addRoutes(context.name()).build();
        if (payload.hasRequest()) {
            handleRequest(msg, payload, payload.getRequest());
        } else if (payload.hasBroadcast()) {
            handleBroadcast(msg, payload, payload.getBroadcast());
        }
    }

    protected void handleBroadcast(Msg msg, Base.Payload payload, Base.Broadcast broadcast) {
        // do nothing
    }

    protected void handleRequest(Msg msg, Base.Payload payload, Base.Request request) {
        logger.debug("on rpc request, id:{}.", request.getId());

        RpcHandler rpcHandler = null;
        AsyncResult<Base.Payload> ar = null;
        try {
            rpcHandler = getHandler(request.getMethod().getApi());

            if (rpcHandler == null) {
                throw ExceptionDefine.NOT_FOUND.build();
            }

            Base.Auth auth = request.getAuth();

            Future<Base.Body> ret = rpcHandler.handle(auth, request.getBody());

            if (ret == null) {
                if (msg.needReply()) {
                    throw new CoreException("方法返回值不能为空, method:" + request.getMethod());
                }
            } else if (ret.isComplete()) {
                Base.Body body = ret.result();
                Base.Response response = CoreXUtil.newResponse(request.getId(), body);
                Base.Payload b = CoreXUtil.newPayload(payload.getId(), response);

                ar = Future.succeededFuture(b);
            } else {
                int requestId = request.getId();
                long payloadId = payload.getId();

                if (msg.needReply()) {
                    ret.addHandler(ar2 -> {
                        if (ar2.succeeded()) {
                            Base.Body body = ret.result();
                            Base.Response response = CoreXUtil.newResponse(requestId, body);
                            Base.Payload b = CoreXUtil.newPayload(payloadId, response);

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

    public Map<String, String> info() {
        Map<String, String> m = new HashMap<>();
        m.put("clz", getClass().getName());
        m.put("addr", name());
        m.put("bc", bc().toString());
        return m;
    }

    private static String rpcHandlerName(RpcHandler rpcHandler) {
        String name;
        return (rpcHandler == null || (name = rpcHandler.name()) == null) ? "未知rpcHandler" : name;
    }

    public int state() {
        return state;
    }

    public void setState(int state) {
        CoreXImpl.ensureContext(context());
        if (state <= this.state) {
            throw new CoreException("服务状态错误,old:" + this.state + ", new:" + state);
        }
        this.state = state;
    }

    protected abstract RpcHandler getHandler(String name);

    protected abstract Module getModule();

}
