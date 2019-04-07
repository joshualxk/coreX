package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.json.JsonObject;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.model.Auth;

/**
 * Created by Joshua on 2018/2/27.
 */
public interface RpcHandler {

    String name();

    boolean isVoidType();

    Callback<Object> handle(Auth auth, JsonObject params) throws Exception;

    // 转换参数
    JsonObjectImpl convert(Object[] args) throws Exception;
}
