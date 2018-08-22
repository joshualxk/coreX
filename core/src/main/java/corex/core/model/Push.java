package corex.core.model;

import corex.core.Joable;
import corex.core.json.JsonObject;

/**
 * Created by Joshua on 2018/8/22
 */
public class Push implements Joable {

    public static final int T = 5;

    private final String topic;
    private final long timestamp;
    private final JsonObject body;

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

    public static Push fromJo(JsonObject jo) throws Exception {
        String topic = jo.getString("t");
        long timestamp = jo.getLong("ts");
        JsonObject body = jo.getJsonObject("b");
        return new Push(topic, timestamp, body);
    }

    @Override
    public JsonObject toJo() {
        return new JsonObject()
                .put("t", topic)
                .put("ts", timestamp)
                .put("b", body);
    }
}
