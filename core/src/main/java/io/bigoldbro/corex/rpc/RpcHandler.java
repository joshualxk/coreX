package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.proto.Base;

/**
 * Created by Joshua on 2018/2/27.
 */
public interface RpcHandler {

    String name();

    MethodDetail methodDetail();

    Future<Base.Body> handle(Base.Auth auth, Base.Body params);

    // 转换参数
    Base.Body convert(Object[] args);
}
