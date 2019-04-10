package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Notice;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.define.ConstDefine;

import java.lang.reflect.Method;

/**
 * Created by Joshua on 2018/3/23.
 */
public class ServerModuleScanner extends ModuleScanner {

    private final Object invoker;

    public ServerModuleScanner(Object invoker) {
        this.invoker = invoker;
    }

    @Override
    protected Object invoker(Class<?> clz) {
        if (!clz.isInstance(invoker)) {
            throw new CoreException("invoker 不是 " + clz.getName() + "的实例");
        }
        return invoker;
    }

    @Override
    protected RpcHandler newApiHandler(Api api, Method m, Object invoker) {
        MethodDetail methodDetail = ParamParser.parseMethodDetail(m);
        checkValidType(api, methodDetail);
        return new ServerRpcHandler(methodDetail, invoker, api.type());
    }

    @Override
    protected RpcHandler newBroadcastHandler(Notice notice, Method m, Object invoker) {
        MethodDetail methodDetail = ParamParser.parseMethodDetail(m);
        return new ServerRpcHandler(methodDetail, invoker, ConstDefine.AUTH_TYPE_INTERNAL);
    }

}
