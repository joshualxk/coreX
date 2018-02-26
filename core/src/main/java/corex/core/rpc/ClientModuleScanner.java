package corex.core.rpc;

import corex.core.annotation.Api;
import corex.core.annotation.Broadcast;
import corex.core.exception.CoreException;

import java.lang.reflect.Method;

/**
 * Created by Joshua on 2018/2/27.
 */
class ClientModuleScanner extends ModuleScanner {

    private static Object o = new Object();

    @Override
    protected Object invoker(Class<?> clz) {
        return o;
    }

    @Override
    protected RpcHandler newApiHandler(Api api, Method m, Object invoker) {
        MethodParamDetail methodParamDetail = new ParamParser().parseMethodParamDetail(m);
        checkValidType(api, methodParamDetail);
        return new ClientRpcHandler(methodParamDetail);
    }

    @Override
    protected RpcHandler newBroadcastHandler(Broadcast broadcast, Method m, Object invoker) {
        MethodParamDetail methodParamDetail = new ParamParser().parseMethodParamDetail(m);
        return new ClientRpcHandler(methodParamDetail);
    }

}
