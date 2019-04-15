package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Notice;
import io.bigoldbro.corex.define.ConstDefine;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Created by Joshua on 2018/3/23.
 */
public class ServerModuleScanner extends ModuleScanner {

    private final Object invoker;

    public ServerModuleScanner(Object invoker, Class<?> clz) {
        super(clz);
        this.invoker = Objects.requireNonNull(invoker);
    }

    @Override
    protected RpcHandler newApiHandler(Api api, Method m) {
        MethodDetail methodDetail = ModuleParser.parseMethodDetail(m);
        checkValidType(api, methodDetail);
        return new ServerRpcHandler(methodDetail, invoker, api.type());
    }

    @Override
    protected RpcHandler newBroadcastHandler(Notice notice, Method m) {
        MethodDetail methodDetail = ModuleParser.parseMethodDetail(m);
        return new ServerRpcHandler(methodDetail, invoker, ConstDefine.AUTH_TYPE_INTERNAL);
    }

}
