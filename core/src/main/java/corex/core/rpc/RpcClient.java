package corex.core.rpc;

import corex.core.*;
import corex.core.annotation.Api;
import corex.core.annotation.Broadcast;
import corex.core.annotation.Module;
import corex.core.define.ConstDefine;
import corex.core.define.ExceptionDefine;
import corex.core.exception.CoreException;
import corex.core.impl.DummyFutureMo;
import corex.core.impl.ReadOnlyFutureMo;
import corex.core.utils.CoreXUtil;
import corex.core.utils.PackageUtil;
import corex.module.BroadcastModule;
import corex.proto.ModelProto;
import corex.proto.ModelProto.Payload;
import corex.proto.ModelProto.RpcRequest;
import corex.proto.ModelProto.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joshua on 2018/3/9.
 */
public class RpcClient {

    private final CoreX coreX;
    private final Map<String, Object> apiProxies = new HashMap<>();
    private BroadcastModule broadcastProxy;

    public RpcClient(CoreX coreX, String packageName) throws Exception {
        this.coreX = coreX;
        init(packageName);
    }

    private void init(String packageName) throws Exception {

        ModuleScanner moduleScanner = ModuleScanner.clientModuleScanner();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (Class<?> clz : PackageUtil.getClassesForPackage(packageName)) {
            Module module = clz.getAnnotation(Module.class);
            if (module == null) {
                continue;
            }

            ModuleParams moduleParams = moduleScanner.parse(clz);

            if (clz == BroadcastModule.class) {
                Object proxy = Proxy.newProxyInstance(cl, new Class<?>[]{clz}, new BroadcastInvocationHandler(coreX, moduleParams));
                if (!clz.isInstance(proxy)) {
                    throw new CoreException("proxy 不是" + clz.getName() + "的实例");
                }

                broadcastProxy = (BroadcastModule) proxy;
            } else if (moduleParams.size() > 0) {
                Object proxy = Proxy.newProxyInstance(cl, new Class<?>[]{clz}, new ApiInvocationHandler(coreX, moduleParams));
                if (!clz.isInstance(proxy)) {
                    throw new CoreException("proxy 不是" + clz.getName() + "的实例");
                }

                if (apiProxies.putIfAbsent(module.address(), proxy) != null) {
                    throw new CoreException("Module名重复:" + module.address());
                }
            }

        }
    }

    @SuppressWarnings("unchecked")
    public <T> T apiAgent(Class<T> clz) {
        Module module = clz.getAnnotation(Module.class);
        if (module == null) {
            throw new CoreException("不支持的类型:" + clz.getName());
        }

        return (T) apiProxies.get(module.address());
    }

    @SuppressWarnings("unchecked")
    public BroadcastModule broadcastAgent() {
        return broadcastProxy;
    }

    private static class ApiInvocationHandler implements InvocationHandler {
        private final CoreX coreX;
        private final ModuleParams moduleParams;

        public ApiInvocationHandler(CoreX coreX, ModuleParams moduleParams) {
            this.coreX = coreX;
            this.moduleParams = moduleParams;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Api api = method.getAnnotation(Api.class);
            if (api == null) {
                throw new CoreException("不支持的方法:" + method.getName());
            }

            if (api.type() != ConstDefine.AUTH_TYPE_INTERNAL) {
                throw new CoreException("非内部调用api");
            }

            RpcHandler rpcHandler = moduleParams.getHandler(api.value());
            if (rpcHandler == null) {
                throw new CoreException("找不到方法:" + method.getName());
            }

            boolean isVoidType = rpcHandler.isVoidType();
            try {
                FutureMo futureMapObject = rpcHandler.convert(args);

                RpcRequest rpcRequest = CoreXUtil.internalRpcRequest(1, moduleParams.module().address(), api.value(), moduleParams.module().version(), futureMapObject.toBodyHolder());

                FutureMoHandler responseHandler = null;
                if (!rpcHandler.isVoidType()) {
                    responseHandler = new FutureMoHandler();
                }
                coreX.sendMessage(moduleParams.module().address(), rpcRequest, responseHandler);
                return responseHandler;
            } catch (Throwable t) {
                if (isVoidType) throw t;
                return new FailedFutureMoHandler(t);
            }
        }
    }

    private static class BroadcastInvocationHandler implements InvocationHandler {
        private final CoreX coreX;
        private final ModuleParams moduleParams;

        public BroadcastInvocationHandler(CoreX coreX, ModuleParams moduleParams) {
            this.coreX = coreX;
            this.moduleParams = moduleParams;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Broadcast broadcast = method.getAnnotation(Broadcast.class);
            if (broadcast == null) {
                throw new CoreException("不支持的方法:" + method.getName());
            }

            RpcHandler rpcHandler = moduleParams.getHandler(broadcast.topic());
            if (rpcHandler == null) {
                throw new CoreException("找不到方法:" + method.getName());
            }

            FutureMo futureMapObject = rpcHandler.convert(args);

            ModelProto.Broadcast b = CoreXUtil.internalBroadcast(broadcast.role(), broadcast.topic(), futureMapObject.toBodyHolder());

            coreX.broadcastMessage(b);
            return null;
        }
    }

    private static class FailedFutureMoHandler extends DummyFutureMo {

        private final Future<Mo> future;

        public FailedFutureMoHandler(Throwable th) {
            future = Future.failedFuture(th);
        }

        @Override
        public void addListener(Handler<AsyncResult<Mo>> handler) {
            future.setHandler(handler);
        }
    }

    private static class FutureMoHandler extends DummyFutureMo implements Handler<AsyncResult<Object>> {

        private final Future<Mo> future = Future.future();

        @Override
        public void handle(AsyncResult<Object> ar) {
            if (ar.succeeded()) {
                if (!(ar.result() instanceof Payload)) {
                    future.fail(new CoreException("未知类型"));
                    return;
                }

                Payload payload = (Payload) ar.result();
                if (payload.hasRpcResponse()) {
                    RpcResponse rpcResponse = payload.getRpcResponse();
                    if (CoreXUtil.isSuccessResponse(rpcResponse)) {
                        FutureMo futureMapObject = new ReadOnlyFutureMo(rpcResponse.getBody());
                        future.complete(futureMapObject);
                    } else {
                        future.fail(ExceptionDefine.newException(rpcResponse.getCode(), rpcResponse.getMsg()));
                    }

                } else {
                    future.fail(new CoreException("payload中不包含response"));
                }
            } else {
                future.fail(ar.cause());
            }
        }

        @Override
        public void addListener(Handler<AsyncResult<Mo>> handler) {
            future.setHandler(handler);
        }
    }

}
