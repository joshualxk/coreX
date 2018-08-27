package corex.core;

import corex.core.json.JsonArray;
import corex.core.json.JsonObject;

import java.util.List;

/**
 * Created by Joshua on 2018/3/8.
 */
public interface Joable {

    JsonObject toJo();

    static JsonArray list2Ja(List<? extends Joable> list) {
        JsonArray ja = new JsonArray();
        if (list != null) {
            list.forEach(e -> {
                ja.add(e.toJo());
            });
        }
        return ja;
    }
}
