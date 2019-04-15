package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Notice;

import java.lang.reflect.Method;

/**
 * Created by Joshua on 2018/2/27.
 */
class ClientModuleScanner extends ModuleScanner {

    public ClientModuleScanner(Class<?> clz) {
        super(clz);
    }

    @Override
    protected RpcHandler newApiHandler(Api api, Method m) {
        MethodDetail methodDetail = ModuleParser.parseMethodDetail(m);
        checkValidType(api, methodDetail);
        return new ClientRpcHandler(methodDetail);
    }

    @Override
    protected RpcHandler newBroadcastHandler(Notice notice, Method m) {
        MethodDetail methodDetail = ModuleParser.parseMethodDetail(m);
        return new ClientRpcHandler(methodDetail);
    }

}
