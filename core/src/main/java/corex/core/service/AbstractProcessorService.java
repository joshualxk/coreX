package corex.core.service;

import corex.core.*;
import corex.core.annotation.Module;
import corex.core.define.ExceptionDefine;
import corex.core.exception.BizException;
import corex.core.exception.CoreException;
import corex.core.impl.ReadOnlyFutureMo;
import corex.core.rpc.BlockControl;
import corex.core.rpc.RpcHandler;
import corex.core.utils.CoreXUtil;
import corex.proto.ModelProto.*;
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
        Object body = msg.detach();

        if (body instanceof Payload) {
            Payload payload = (Payload) body;
            payload = payload.toBuilder().addRoutes(context.name()).build();

            if (payload.hasRpcRequest()) {
                handleRequest(msg, payload, payload.getRpcRequest());
            } else if (payload.hasBroadcast()) {
                handleBroadcast(msg, payload, payload.getBroadcast());
            }
        }
    }

    protected void handleBroadcast(Msg msg, Payload payload, Broadcast broadcast) {
        // do nothing
    }

    protected void handleRequest(Msg msg, Payload payload, RpcRequest request) {
        logger.debug("on rpc request, id:{}.", request.getId());

        RpcHandler rpcHandler = null;
        AsyncResult<Object> ar = null;
        try {
            rpcHandler = getHandler(request.getMethod().getApi());

            if (rpcHandler == null) {
                throw ExceptionDefine.NOT_FOUND.build();
            }

            Auth auth = request.getAuth();

            ReadOnlyFutureMo params = new ReadOnlyFutureMo(request.getBody());

            FutureMo ret = rpcHandler.handle(auth, params);

            if (ret == null) {
                if (msg.needReply()) {
                    throw new CoreException("方法返回值不能为空, method:" + request.getMethod());
                }
            } else if (ret instanceof AsyncFutureMo) {
                AsyncFutureMo asyncFutureMo = (AsyncFutureMo) ret;
                int requestId = request.getId();
                long payloadId = payload.getId();

                if (msg.needReply()) {
                    asyncFutureMo.setHandler(ar2 -> {
                        if (ar2.succeeded()) {
                            FutureMo ret2 = ar2.result();
                            RpcResponse rpcResponse = CoreXUtil.newRpcResponse(requestId, ret2.toBodyHolder());
                            Payload b = CoreXUtil.assemblePayload(payloadId, rpcResponse).build();
                            msg.reply(Future.succeededFuture(b));
                        } else {
                            msg.reply(Future.failedFuture(ar2.cause()));
                        }
                    });
                }

                return;
            } else {
                RpcResponse rpcResponse = CoreXUtil.newRpcResponse(request.getId(), ret.toBodyHolder());
                Payload b = CoreXUtil.assemblePayload(payload.getId(), rpcResponse).build();
                ar = Future.succeededFuture(b);
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

    protected FutureMo baseInfo() {
        FutureMo ret = FutureMo.futureMo();
        ret.putString("clzName", getClass().getName());
        ret.putString("address", name());
        ret.putString("bc", bc().toString());
        return ret;
    }

    private static String rpcHandlerName(RpcHandler rpcHandler) {
        String name;
        return (rpcHandler == null || (name = rpcHandler.name()) == null) ? "未知rpcHandler" : name;
    }

    protected abstract RpcHandler getHandler(String name);

    protected abstract Module getModule();

}
