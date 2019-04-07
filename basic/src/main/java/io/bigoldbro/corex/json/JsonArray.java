package io.bigoldbro.corex.json;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Joshua on 2019/4/7
 */
public interface JsonArray extends Iterable<Object> {

    String getString(int pos);

    Integer getInteger(int pos);

    Long getLong(int pos);

    Double getDouble(int pos);

    Float getFloat(int pos);

    Boolean getBoolean(int pos);

    JsonObject getJsonObject(int pos);

    JsonArray getJsonArray(int pos);

    byte[] getBinary(int pos);

    Instant getInstant(int pos);

    Object getValue(int pos);

    boolean hasNull(int pos);

    JsonArray add(Enum value);

    JsonArray add(CharSequence value);

    JsonArray add(String value);

    JsonArray add(Integer value);

    JsonArray add(Long value);

    JsonArray add(Double value);

    JsonArray add(Float value);

    JsonArray add(Boolean value);

    JsonArray addNull();

    JsonArray add(JsonObject value);

    JsonArray add(JsonArray value);

    JsonArray add(byte[] value);

    JsonArray add(Instant value);

    JsonArray add(Object value);

    JsonArray addAll(JsonArray array);

    boolean contains(Object value);

    boolean remove(Object value);

    Object remove(int pos);

    int size();

    boolean isEmpty();

    List getList();

    JsonArray clear();

    String encode();

    String encodePrettily();

    JsonArray copy();

    Stream<Object> stream();

}
