package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Notice;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;
import io.bigoldbro.corex.exception.CoreException;
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

        Notice notice = method.getAnnotation(Notice.class);
        if (notice != null) {
            RpcHandler handler = newBroadcastHandler(notice, method, invoker);
            if (handlerMap.putIfAbsent(notice.topic(), handler) != null) {
                throw new CoreException("notice 名字不能重复:" + handler.name());
            }
        }

    }

    private static void validateModule(Module module) {
        if (!ServiceNameDefine.isValidName(module.address())) {
            throw new CoreException("模块地址不合法");
        }

        if (StringUtil.isNullOrEmpty(module.version())) {
            throw new CoreException("模块版本号不能为空");
        }
    }

    private void validateApi(Api api) {
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

    protected static void checkValidType(Api api, MethodDetail methodDetail) {
        if (methodDetail.returnDetail.isVoid &&
                (api.type() == ConstDefine.AUTH_TYPE_NON || api.type() == ConstDefine.AUTH_TYPE_CLIENT)) {
            throw new CoreException("来自玩家的调用不能返回void:" + methodDetail.name());
        }
    }

    protected abstract Object invoker(Class<?> clz);

    protected abstract RpcHandler newApiHandler(Api api, Method m, Object invoker);

    protected abstract RpcHandler newBroadcastHandler(Notice notice, Method m, Object invoker);

}
