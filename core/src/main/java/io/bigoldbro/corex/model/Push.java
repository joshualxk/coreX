package io.bigoldbro.corex.model;

import io.bigoldbro.corex.json.Joable;
import io.bigoldbro.corex.json.JsonObject;

/**
 * Created by Joshua on 2018/8/22
 */
public class Push implements Joable {

    public static final int T = 5;

    private String topic;
    private long timestamp;
    private JsonObject body;

    private Push(String topic, long timestamp, JsonObject body) {
        this.topic = topic;
        this.timestamp = timestamp;
        this.body = body;
    }

    public static Push newPush(String topic, long timestamp, JsonObject body) {
        return new Push(topic, timestamp, body);
    }

    public String getTopic() {
        return topic;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public JsonObject getBody() {
        return body;
    }

    @Override
    public void readFrom(JsonObject jo) {
        topic = jo.getString("t");
        timestamp = jo.getLong("ts");
        body = jo.getJsonObject("b");
    }

    @Override
    public void writeTo(JsonObject jo) {
        jo.put("t", topic)
                .put("ts", timestamp)
                .put("b", body);
    }
}
