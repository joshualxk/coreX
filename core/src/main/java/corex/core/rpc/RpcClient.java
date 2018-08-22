package corex.core.rpc;

import corex.core.AsyncResult;
import corex.core.CoreX;
import corex.core.Handler;
import corex.core.JoHolder;
import corex.core.annotation.Api;
import corex.core.annotation.Module;
import corex.core.annotation.Notice;
import corex.core.define.ConstDefine;
import corex.core.define.ExceptionDefine;
import corex.core.exception.CoreException;
import corex.core.impl.AsyncJoHolder;
import corex.core.json.JsonObject;
import corex.core.model.Payload;
import corex.core.model.RpcRequest;
import corex.core.model.RpcResponse;
import corex.core.utils.CoreXUtil;
import corex.core.utils.PackageUtil;
import corex.module.BroadcastModule;

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
            return invoke0(proxy, method, args);
        }

        private JoHolder invoke0(Object proxy, Method method, Object[] args) throws Throwable {
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
                JsonObject jo = rpcHandler.convert(args);

                RpcRequest rpcRequest = RpcRequest.internalRpcRequest(1, moduleParams.module().address(), api.value(), moduleParams.module().version(), jo);

                JoHolder joHolder = null;
                Handler<AsyncResult<Payload>> replyHandler = null;

                if (!rpcHandler.isVoidType()) {
                    AsyncJoHolder asyncJoHolder = JoHolder.newAsync();
                    replyHandler = newPayloadHandler(asyncJoHolder);
                    joHolder = asyncJoHolder;
                }
                coreX.sendMessage(moduleParams.module().address(), rpcRequest, replyHandler);
                return joHolder;
            } catch (Throwable t) {
                if (isVoidType) throw t;
                return JoHolder.newFailedAsync(t);
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
            Notice notice = method.getAnnotation(Notice.class);
            if (notice == null) {
                throw new CoreException("不支持的方法:" + method.getName());
            }

            RpcHandler rpcHandler = moduleParams.getHandler(notice.topic());
            if (rpcHandler == null) {
                throw new CoreException("找不到方法:" + method.getName());
            }

            JsonObject jo = rpcHandler.convert(args);

            corex.core.model.Broadcast b = corex.core.model.Broadcast.newInternalBroadcast(notice.role(), notice.topic(), jo);

            coreX.broadcastMessage(b);
            return null;
        }
    }

    private static Handler<AsyncResult<Payload>> newPayloadHandler(AsyncJoHolder asyncJoHolder) {
        return ar -> {
            if (ar.succeeded()) {

                Payload payload = ar.result();
                if (payload.hasRpcResponse()) {
                    RpcResponse rpcResponse = payload.getRpcResponse();
                    if (CoreXUtil.isSuccessResponse(rpcResponse)) {
                        asyncJoHolder.complete(JoHolder.newSync(rpcResponse.getBody()));
                    } else {
                        asyncJoHolder.fail(ExceptionDefine.newException(rpcResponse.getCode(), rpcResponse.getMessage()));
                    }

                } else {
                    asyncJoHolder.fail(new CoreException("payload中不包含response"));
                }
            } else {
                asyncJoHolder.fail(ar.cause());
            }
        };
    }

}
