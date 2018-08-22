package corex.core.model;

import corex.core.Joable;
import corex.core.exception.BizEx;
import corex.core.json.JsonObject;
import corex.core.utils.CoreXUtil;

/**
 * Created by Joshua on 2018/8/22
 */
public class RpcResponse implements Joable {

    public static final int T = 4;

    private final int id;
    private final int code;
    private final String message;
    private final long timestamp;
    private final JsonObject body;

    private RpcResponse(int id, int code, String message, long timestamp, JsonObject body) {
        this.id = id;
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.body = body;
    }

    public static RpcResponse newRpcResponse(int id, int code, String message, long timestamp, JsonObject body) {
        return new RpcResponse(id, code, message, timestamp, body);
    }

    public static RpcResponse newSuccessRpcResponse(int id, JsonObject body) {
        return newRpcResponse(id, 0, "", CoreXUtil.sysTime(), body);
    }

    public static RpcResponse newSuccessRpcResponse(int id) {
        return newRpcResponse(id, 0, "", CoreXUtil.sysTime(), new JsonObject());
    }

    public static RpcResponse newBizExRpcResponse(int id, BizEx bizEx) {
        return newRpcResponse(id, bizEx.getCode(), bizEx.getMessage(), CoreXUtil.sysTime(), null);
    }

    public int getId() {
        return id;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public JsonObject getBody() {
        return body;
    }

    public static RpcResponse fromJo(JsonObject jo) {
        int id = jo.getInteger("id");
        int code = jo.getInteger("code");
        String message = jo.getString("msg");
        long timestamp = jo.getLong("ts");
        JsonObject body = jo.getJsonObject("b");
        return new RpcResponse(id, code, message, timestamp, body);
    }

    @Override
    public JsonObject toJo() {
        return new JsonObject()
                .put("id", id)
                .put("code", code)
                .put("msg", message)
                .put("ts", timestamp)
                .put("b", body);
    }
}
