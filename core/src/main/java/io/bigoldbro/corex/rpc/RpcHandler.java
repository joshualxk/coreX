package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.proto.Base;

/**
 * Created by Joshua on 2018/2/27.
 */
public interface RpcHandler {

    String name();

    boolean isVoidType();

    Callback<Base.Body> handle(Base.Auth auth, Base.Body params) throws Exception;

    // 转换参数
    Base.Body convert(Object[] args) throws Exception;
}
