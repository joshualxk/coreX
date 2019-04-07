package io.bigoldbro.corex.model;

import io.bigoldbro.corex.json.JsonObjectImpl;

/**
 * Created by Joshua on 2018/8/21
 * 服务器间验证身份
 */
public class ServerAuth implements Joable {

    public static final int T = 1;

    public final int id;
    public final int role;
    public final long startTime;
    public final long timestamp;

    public ServerAuth(int id, int role, long startTime, long timestamp) {
        this.id = id;
        this.role = role;
        this.startTime = startTime;
        this.timestamp = timestamp;
    }

    public static ServerAuth fromJo(JsonObjectImpl jo) throws Exception {
        int id = jo.getInteger("id");
        int role = jo.getInteger("r");
        long startTime = jo.getLong("st");
        long timestamp = jo.getLong("ts");
        return new ServerAuth(id, role, startTime, timestamp);
    }

    @Override
    public JsonObjectImpl toJo() {
        return new JsonObjectImpl()
                .put("id", id)
                .put("r", role)
                .put("st", startTime)
                .put("ts", timestamp);
    }
}
