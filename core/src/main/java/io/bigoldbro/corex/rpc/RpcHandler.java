package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.model.Auth;

/**
 * Created by Joshua on 2018/2/27.
 */
public interface RpcHandler {

    String name();

    boolean isVoidType();

    <T> Callback<T> handle(Auth auth, JsonObjectImpl params) throws Exception;

    // 把参数转成proto类型
    JsonObjectImpl convert(Object[] args) throws Exception;
}
