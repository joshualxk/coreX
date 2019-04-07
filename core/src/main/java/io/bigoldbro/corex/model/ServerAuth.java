package io.bigoldbro.corex.model;

import io.bigoldbro.corex.json.Joable;
import io.bigoldbro.corex.json.JsonObject;

/**
 * Created by Joshua on 2018/8/21
 * 服务器间验证身份
 */
public class ServerAuth implements Joable {

    public static final int T = 1;

    public int id;
    public int role;
    public long startTime;
    public long timestamp;

    public void readFrom(JsonObject jo) {
        id = jo.getInteger("id");
        role = jo.getInteger("r");
        startTime = jo.getLong("st");
        timestamp = jo.getLong("ts");
    }

    @Override
    public void writeTo(JsonObject jo) {
        jo.put("id", id)
                .put("r", role)
                .put("st", startTime)
                .put("ts", timestamp);
    }
}
