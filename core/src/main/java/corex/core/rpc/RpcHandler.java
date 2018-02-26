package corex.core.rpc;

import corex.core.FutureMo;
import corex.core.Mo;
import corex.proto.ModelProto.Auth;

/**
 * Created by Joshua on 2018/2/27.
 */
public interface RpcHandler {

    String name();

    boolean isVoidType();

    FutureMo handle(Auth auth, Mo params) throws Exception;

    // 把参数转成proto类型
    FutureMo convert(Object[] args) throws Exception;
}
