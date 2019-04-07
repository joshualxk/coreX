package io.bigoldbro.corex.json;

import java.io.OutputStream;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by Joshua on 2019/4/7
 */
public interface JsonObject extends Iterable<Map.Entry<String, Object>> {

    String getString(String key);

    Integer getInteger(String key);

    Long getLong(String key);

    Double getDouble(String key);

    Float getFloat(String key);

    Boolean getBoolean(String key);

    JsonObject getJsonObject(String key);

    JsonArray getJsonArray(String key);

    <T extends Joable> T getJoable(String key, Class<T> clz) throws Exception;

    byte[] getBinary(String key);

    Instant getInstant(String key);

    Object getValue(String key);

    String getString(String key, String def);

    Integer getInteger(String key, Integer def);

    Long getLong(String key, Long def);

    Double getDouble(String key, Double def);

    Float getFloat(String key, Float def);

    Boolean getBoolean(String key, Boolean def);

    JsonObject getJsonObject(String key, JsonObject def);

    JsonArray getJsonArray(String key, JsonArray def);

    byte[] getBinary(String key, byte[] def);

    Object getValue(String key, Object def);

    boolean containsKey(String key);

    Set<String> fieldNames();

    JsonObject put(String key, Enum value);

    JsonObject put(String key, CharSequence value);

    JsonObject put(String key, String value);

    JsonObject put(String key, Integer value);

    JsonObject put(String key, Long value);

    JsonObject put(String key, Double value);

    JsonObject put(String key, Float value);

    JsonObject put(String key, Boolean value);

    JsonObject putNull(String key);

    JsonObject put(String key, JsonObject value);

    JsonObject put(String key, JsonArray value);

    JsonObject put(String key, byte[] value);

    JsonObject put(String key, Instant value);

    JsonObject put(String key, Joable value);

    JsonObject put(String key, Object value);

    Object remove(String key);

    JsonObject mergeIn(JsonObject other);

    JsonObject mergeIn(JsonObject other, boolean deep);

    JsonObject mergeIn(JsonObject other, int depth);

    String encode();

    void encode(OutputStream os);

    String encodePrettily();

    JsonObject copy();

    Map<String, Object> getMap();

    Stream<Map.Entry<String, Object>> stream();

    int size();

    JsonObject clear();

    boolean isEmpty();

    String getStringParam(String key);

    int getIntegerParam(String key);

}
