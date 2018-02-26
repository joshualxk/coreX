package corex.core.rpc;

import corex.core.annotation.Api;
import corex.core.annotation.Broadcast;
import corex.core.define.ConstDefine;
import corex.core.exception.CoreException;

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
        MethodParamDetail methodParamDetail = new ParamParser().parseMethodParamDetail(m);
        checkValidType(api, methodParamDetail);
        return new ServerRpcHandler(methodParamDetail, invoker, api.type());
    }

    @Override
    protected RpcHandler newBroadcastHandler(Broadcast broadcast, Method m, Object invoker) {
        MethodParamDetail methodParamDetail = new ParamParser().parseMethodParamDetail(m);
        return new ServerRpcHandler(methodParamDetail, invoker, ConstDefine.AUTH_TYPE_INTERNAL);
    }

}
