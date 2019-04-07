package io.bigoldbro.corex.model;

import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.json.Joable;
import io.bigoldbro.corex.json.JsonObject;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.utils.CoreXUtil;

import java.util.Objects;

/**
 * Created by Joshua on 2018/8/22
 */
public class RpcRequest implements Joable {

    public static final int T = 3;

    private int id;
    private int type;
    private Method method;
    private Auth auth;
    private long timestamp;
    private String body;

    private RpcRequest(int id, int type, Method method, Auth auth, long timestamp, JsonObject body) {
        this.id = id;
        this.type = type;
        this.method = Objects.requireNonNull(method);
        this.auth = Objects.requireNonNull(auth);
        this.timestamp = timestamp;
        this.body = Objects.requireNonNull(body).encode();
    }

    public RpcRequest authorize(String token) {
        auth = Auth.newAuth(ConstDefine.AUTH_TYPE_CLIENT, token);
        return this;
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

    public JsonObject getBody() {
        return new JsonObjectImpl(body);
    }

    public void readFrom(JsonObject jo) {
        id = jo.getInteger("id");
        type = jo.getInteger("t");
        method = jo.getJoable("m", Method.class);
        auth = jo.getJoable("a", Auth.class);
        timestamp = jo.getLong("ts");
        body = jo.getString("bs");
    }

    @Override
    public void writeTo(JsonObject jo) {
        jo.put("id", id)
                .put("t", type)
                .put("m", method)
                .put("a", auth)
                .put("ts", timestamp)
                .put("b", body);
    }
}
