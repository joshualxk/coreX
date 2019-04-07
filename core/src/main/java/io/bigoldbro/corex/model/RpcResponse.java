package io.bigoldbro.corex.model;

import io.bigoldbro.corex.exception.BizEx;
import io.bigoldbro.corex.json.Joable;
import io.bigoldbro.corex.json.JsonObject;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.utils.CoreXUtil;

/**
 * Created by Joshua on 2018/8/22
 */
public class RpcResponse implements Joable {

    public static final int T = 4;

    private int id;
    private int code;
    private String message;
    private long timestamp;
    private JsonObject body;

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

    public static RpcResponse newSuccessRpcResponse(int id, JsonObjectImpl body) {
        return newRpcResponse(id, 0, "", CoreXUtil.sysTime(), body);
    }

    public static RpcResponse newSuccessRpcResponse(int id) {
        return newRpcResponse(id, 0, "", CoreXUtil.sysTime(), new JsonObjectImpl());
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

    public void readFrom(JsonObject jo) {
        id = jo.getInteger("id");
        code = jo.getInteger("code");
        message = jo.getString("msg");
        timestamp = jo.getLong("ts");
        body = jo.getJsonObject("b");
    }

    @Override
    public void writeTo(JsonObject jo) {
        jo.put("id", id)
                .put("code", code)
                .put("msg", message)
                .put("ts", timestamp)
                .put("b", body);
    }
}
