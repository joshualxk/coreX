package corex.core.rpc;

import corex.core.annotation.Api;
import corex.core.annotation.Broadcast;
import corex.core.annotation.Module;
import corex.core.define.ConstDefine;
import corex.core.define.ServiceNameDefine;
import corex.core.exception.CoreException;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Joshua on 2018/2/27.
 */
public abstract class ModuleScanner {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public static ModuleScanner clientModuleScanner() {
        return new ClientModuleScanner();
    }

    public ModuleParams parse(Class<?> clz) {
//        logger.info(">>>> start parse:{}.", clz.getName());

        Module module = Objects.requireNonNull(clz.getAnnotation(Module.class));

        if (!clz.isInterface()) {
            throw new CoreException(clz.getName() + "不是接口类");
        }

        validateModule(module);

        Method[] methods = clz.getDeclaredMethods();
        if (methods.length == 0) {
            throw new CoreException("api 数量为零");
        }

        Object invoker = Objects.requireNonNull(invoker(clz));

        Map<String, RpcHandler> apiHandlerMap = new HashMap<>();
        for (Method method : clz.getDeclaredMethods()) {
            parseMethod(apiHandlerMap, invoker, method);
        }

//        logger.info(">>>> end parse:{}.", clz.getName());

        return new ModuleParams(module, apiHandlerMap);
    }

    private void parseMethod(Map<String, RpcHandler> handlerMap, Object invoker, Method method) {
        Api api = method.getAnnotation(Api.class);
        if (api != null) {
            validateApi(api);

            RpcHandler handler = newApiHandler(api, method, invoker);
            if (handlerMap.putIfAbsent(api.value(), handler) != null) {
                throw new CoreException("api 名字不能重复:" + handler.name());
            }
        }

        Broadcast broadcast = method.getAnnotation(Broadcast.class);
        if (broadcast != null) {
            RpcHandler handler = newBroadcastHandler(broadcast, method, invoker);
            if (handlerMap.putIfAbsent(broadcast.topic(), handler) != null) {
                throw new CoreException("broadcast 名字不能重复:" + handler.name());
            }
        }

    }

    public static void validateModule(Module module) {
        if (!ServiceNameDefine.isValidName(module.address())) {
            throw new CoreException("模块地址不合法");
        }

        if (StringUtil.isNullOrEmpty(module.version())) {
            throw new CoreException("模块版本号不能为空");
        }
    }

    public void validateApi(Api api) {
        if (api.type() != ConstDefine.AUTH_TYPE_NON
                && api.type() != ConstDefine.AUTH_TYPE_CLIENT
                && api.type() != ConstDefine.AUTH_TYPE_ADMIN
                && api.type() != ConstDefine.AUTH_TYPE_INTERNAL) {
            throw new CoreException("接口授权类型不正确:" + api.type());
        }

        if (StringUtil.isNullOrEmpty(api.value())) {
            throw new CoreException("接口名字不能为空");
        }
    }

    protected static void checkValidType(Api api, MethodParamDetail methodParamDetail) {
        if (methodParamDetail.isVoidType &&
                (api.type() == ConstDefine.AUTH_TYPE_NON || api.type() == ConstDefine.AUTH_TYPE_CLIENT)) {
            throw new CoreException("来自玩家的调用不能返回void:" + methodParamDetail.name());
        }
    }

    protected abstract Object invoker(Class<?> clz);

    protected abstract RpcHandler newApiHandler(Api api, Method m, Object invoker);

    protected abstract RpcHandler newBroadcastHandler(Broadcast broadcast, Method m, Object invoker);

}
