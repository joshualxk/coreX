package io.bigoldbro.corex.model;

import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.utils.CoreXUtil;

import java.util.Objects;

/**
 * Created by Joshua on 2018/8/22
 */
public class RpcRequest implements Joable {

    public static final int T = 3;

    private final int id;
    private final int type;
    private final Method method;
    private final Auth auth;
    private final long timestamp;
    private final JsonObjectImpl body;

    private RpcRequest(int id, int type, Method method, Auth auth, long timestamp, JsonObjectImpl body) {
        this.id = id;
        this.type = type;
        this.method = Objects.requireNonNull(method);
        this.auth = Objects.requireNonNull(auth);
        this.timestamp = timestamp;
        this.body = Objects.requireNonNull(body);
    }

    public RpcRequest authorize(String token) {
        return new RpcRequest(id, type, method, Auth.newAuth(ConstDefine.AUTH_TYPE_CLIENT, token), timestamp, body);
    }

    private static RpcRequest newRpcRequest(int id, int type, Method method, Auth auth, long timestamp, JsonObjectImpl body) {
        return new RpcRequest(id, type, method, auth, timestamp, body);
    }

    public static RpcRequest internalRpcRequest(int id, String module, String api, String version, JsonObjectImpl body) {
        return newRpcRequest(id, 0, Method.newMethod(module, api, version), Auth.internalAuth(), CoreXUtil.sysTime(), body);
    }

    public static RpcRequest newAdminRpcRequest(int id, String module, String api, String version, JsonObjectImpl body) {
        return newRpcRequest(id, 0, Method.newMethod(module, api, version), Auth.adminAuth(), CoreXUtil.sysTime(), body);
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public Method getMethod() {
        return method;
    }

    public Auth getAuth() {
        return auth;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public JsonObjectImpl getBody() {
        return body;
    }

    public static RpcRequest fromJo(JsonObjectImpl jo) throws Exception {
        int id = jo.getInteger("id");
        int type = jo.getInteger("t");
        Method method = Method.fromJo(jo.getJsonObject("m"));
        Auth auth = Auth.fromJo(jo.getJsonObject("a"));
        long timestamp = jo.getLong("ts");
        JsonObjectImpl body = jo.getJsonObject("b");
        return new RpcRequest(id, type, method, auth, timestamp, body);
    }

    @Override
    public JsonObjectImpl toJo() {
        return new JsonObjectImpl()
                .put("id", id)
                .put("t", type)
                .put("m", method.toJo())
                .put("a", auth.toJo())
                .put("ts", timestamp)
                .put("b", body);
    }
}
