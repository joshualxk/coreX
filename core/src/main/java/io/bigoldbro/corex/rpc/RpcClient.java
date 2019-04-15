package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.AsyncResult;
import io.bigoldbro.corex.CoreX;
import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.Handler;
import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Notice;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.exception.BizException;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.impl.CoreXImpl;
import io.bigoldbro.corex.impl.FailedFuture;
import io.bigoldbro.corex.impl.FutureImpl;
import io.bigoldbro.corex.module.BroadcastModule;
import io.bigoldbro.corex.proto.Base;
import io.bigoldbro.corex.utils.CoreXUtil;
import io.bigoldbro.corex.utils.PackageUtil;

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

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (Class<?> clz : PackageUtil.getClassesForPackage(packageName)) {
            Module module = clz.getAnnotation(Module.class);
            if (module == null) {
                continue;
            }

            ModuleInfo moduleInfo = new ClientModuleScanner(clz).parse();

            if (clz == BroadcastModule.class) {
                Object proxy = Proxy.newProxyInstance(cl, new Class<?>[]{clz}, new BroadcastInvocationHandler(coreX, moduleInfo));

                broadcastProxy = (BroadcastModule) proxy;
            } else if (moduleInfo.size() > 0) {
                Object proxy = Proxy.newProxyInstance(cl, new Class<?>[]{clz}, new ApiInvocationHandler(coreX, moduleInfo));

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
        private final ModuleInfo moduleInfo;

        public ApiInvocationHandler(CoreX coreX, ModuleInfo moduleInfo) {
            this.coreX = coreX;
            this.moduleInfo = moduleInfo;
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

            RpcHandler rpcHandler = moduleInfo.getHandler(api.value());
            if (rpcHandler == null) {
                throw new CoreException("找不到方法:" + method.getName());
            }

            final boolean isVoid = rpcHandler.methodDetail().returnDetail.isVoid;
            final boolean isAsync = rpcHandler.methodDetail().returnDetail.isAsync;

            if (!isAsync) {
                CoreXImpl.ensureBlockSafe();
            }
            try {
                Base.Body body = rpcHandler.convert(args);

                Base.Method m = CoreXUtil.newMethod(moduleInfo.module().address(), api.value(), moduleInfo.module().version());
                Base.Request request = CoreXUtil.newRequest(1, CoreXUtil.internalAuth(), m, body);

                Future<Object> future = null;
                Handler<AsyncResult<Base.Payload>> replyHandler = null;

                if (!isVoid) {
                    future = new FutureImpl<>();
                    replyHandler = newPayloadHandler(rpcHandler.methodDetail().returnDetail, future);
                }
                coreX.sendMessage(moduleInfo.module().address(), request, replyHandler);

                if (!isAsync) {
                    return future.sync().result();
                }
                return future;
            } catch (Throwable t) {
                if (isVoid || !isAsync) throw t;
                return new FailedFuture<>(t);
            }
        }

    }

    private static class BroadcastInvocationHandler implements InvocationHandler {
        private final CoreX coreX;
        private final ModuleInfo moduleInfo;

        public BroadcastInvocationHandler(CoreX coreX, ModuleInfo moduleInfo) {
            this.coreX = coreX;
            this.moduleInfo = moduleInfo;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            Notice notice = method.getAnnotation(Notice.class);
            if (notice == null) {
                throw new CoreException("不支持的方法:" + method.getName());
            }

            RpcHandler rpcHandler = moduleInfo.getHandler(notice.topic());
            if (rpcHandler == null) {
                throw new CoreException("找不到方法:" + method.getName());
            }

            Base.Body body = rpcHandler.convert(args);

            Base.Broadcast b = Base.Broadcast.newBuilder()
                    .setRole(notice.role())
                    .setInternal(true)
                    .setPush(Base.Push.newBuilder()
                            .setTimestamp(System.currentTimeMillis())
                            .setBody(body)
                            .setTopic(notice.topic()))
                    .build();

            coreX.broadcastMessage(b);
            return null;
        }
    }

    private static Handler<AsyncResult<Base.Payload>> newPayloadHandler(ReturnDetail returnDetail, Future<Object> future) {
        return ar -> {
            if (ar.succeeded()) {

                Base.Payload payload = ar.result();
                if (payload.hasResponse()) {
                    Base.Response response = payload.getResponse();
                    if (CoreXUtil.isSuccessResponse(response)) {
                        if (response.getBody().getFieldsCount() < 1) {
                            future.fail(ExceptionDefine.SYSTEM_ERR.build());
                            return;
                        }
                        Object o = ServerRpcHandler.unwrapObj(returnDetail, response.getBody().getFields(0));

                        future.complete(o);
                    } else {
                        future.fail(BizException.newException(response.getCode(), response.getMsg()));
                    }

                } else {
                    future.fail(new CoreException("payload中不包含response"));
                }
            } else {
                future.fail(ar.cause());
            }
        };
    }

}
