package corex.core.rpc;

import corex.core.JoHolder;
import corex.core.json.JsonObject;
import corex.core.model.Auth;

/**
 * Created by Joshua on 2018/2/27.
 */
public interface RpcHandler {

    String name();

    boolean isVoidType();

    JoHolder handle(Auth auth, JsonObject params) throws Exception;

    // 把参数转成proto类型
    JsonObject convert(Object[] args) throws Exception;
}
