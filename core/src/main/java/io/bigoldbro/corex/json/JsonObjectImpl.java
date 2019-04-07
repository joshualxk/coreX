package io.bigoldbro.corex.json;

import io.bigoldbro.corex.define.ExceptionDefine;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

/**
 * Created by Joshua on 2018/8/23.
 */
public class JsonObjectImpl implements JsonObject {

    private Map<String, Object> map;

    public JsonObjectImpl(String json) {
        fromJson(json);
    }

    public JsonObjectImpl(InputStream is) {
        fromJson(is);
    }

    public JsonObjectImpl() {
        map = new LinkedHashMap<>();
    }

    public JsonObjectImpl(Map<String, Object> map) {
        this.map = map;
    }

    @SuppressWarnings("unchecked")
    public static JsonObjectImpl mapFrom(Object obj) {
        return new JsonObjectImpl((Map<String, Object>) Json.mapper.convertValue(obj, Map.class));
    }

    public String getString(String key) {
        Objects.requireNonNull(key);
        CharSequence cs = (CharSequence) map.get(key);
        return cs == null ? null : cs.toString();
    }

    public Integer getInteger(String key) {
        Objects.requireNonNull(key);
        Number number = (Number) map.get(key);
        if (number == null) {
            return null;
        } else if (number instanceof Integer) {
            return (Integer) number;  // Avoids unnecessary unbox/box
        } else {
            return number.intValue();
        }
    }

    public Long getLong(String key) {
        Objects.requireNonNull(key);
        Number number = (Number) map.get(key);
        if (number == null) {
            return null;
        } else if (number instanceof Long) {
            return (Long) number;  // Avoids unnecessary unbox/box
        } else {
            return number.longValue();
        }
    }

    public Double getDouble(String key) {
        Objects.requireNonNull(key);
        Number number = (Number) map.get(key);
        if (number == null) {
            return null;
        } else if (number instanceof Double) {
            return (Double) number;  // Avoids unnecessary unbox/box
        } else {
            return number.doubleValue();
        }
    }

    public Float getFloat(String key) {
        Objects.requireNonNull(key);
        Number number = (Number) map.get(key);
        if (number == null) {
            return null;
        } else if (number instanceof Float) {
            return (Float) number;  // Avoids unnecessary unbox/box
        } else {
            return number.floatValue();
        }
    }

    public Boolean getBoolean(String key) {
        Objects.requireNonNull(key);
        return (Boolean) map.get(key);
    }

    @Override
    public JsonObjectImpl getJsonObject(String key) {
        Objects.requireNonNull(key);
        Object val = map.get(key);
        if (val instanceof Map) {
            val = new JsonObjectImpl((Map) val);
        }
        return (JsonObjectImpl) val;
    }

    @Override
    public JsonArray getJsonArray(String key) {
        Objects.requireNonNull(key);
        Object val = map.get(key);
        if (val instanceof List) {
            val = new JsonArrayImpl((List) val);
        }
        return (JsonArray) val;
    }

    @Override
    public <T extends Joable> T getJoable(String key, Class<T> clz) throws Exception {
        JsonObject jo = getJsonObject(key);
        T t = clz.newInstance();
        t.readFrom(jo);
        return t;
    }

    public byte[] getBinary(String key) {
        Objects.requireNonNull(key);
        String encoded = (String) map.get(key);
        return encoded == null ? null : Base64.getDecoder().decode(encoded);
    }

    public Instant getInstant(String key) {
        Objects.requireNonNull(key);
        String encoded = (String) map.get(key);
        return encoded == null ? null : Instant.from(ISO_INSTANT.parse(encoded));
    }

    public Object getValue(String key) {
        Objects.requireNonNull(key);
        Object val = map.get(key);
        if (val instanceof Map) {
            val = new JsonObjectImpl((Map) val);
        } else if (val instanceof List) {
            val = new JsonArrayImpl((List) val);
        }
        return val;
    }

    public String getString(String key, String def) {
        Objects.requireNonNull(key);
        CharSequence cs = (CharSequence) map.get(key);
        return cs != null || map.containsKey(key) ? cs == null ? null : cs.toString() : def;
    }

    public Integer getInteger(String key, Integer def) {
        Objects.requireNonNull(key);
        Number val = (Number) map.get(key);
        if (val == null) {
            if (map.containsKey(key)) {
                return null;
            } else {
                return def;
            }
        } else if (val instanceof Integer) {
            return (Integer) val;  // Avoids unnecessary unbox/box
        } else {
            return val.intValue();
        }
    }

    public Long getLong(String key, Long def) {
        Objects.requireNonNull(key);
        Number val = (Number) map.get(key);
        if (val == null) {
            if (map.containsKey(key)) {
                return null;
            } else {
                return def;
            }
        } else if (val instanceof Long) {
            return (Long) val;  // Avoids unnecessary unbox/box
        } else {
            return val.longValue();
        }
    }

    public Double getDouble(String key, Double def) {
        Objects.requireNonNull(key);
        Number val = (Number) map.get(key);
        if (val == null) {
            if (map.containsKey(key)) {
                return null;
            } else {
                return def;
            }
        } else if (val instanceof Double) {
            return (Double) val;  // Avoids unnecessary unbox/box
        } else {
            return val.doubleValue();
        }
    }

    public Float getFloat(String key, Float def) {
        Objects.requireNonNull(key);
        Number val = (Number) map.get(key);
        if (val == null) {
            if (map.containsKey(key)) {
                return null;
            } else {
                return def;
            }
        } else if (val instanceof Float) {
            return (Float) val;  // Avoids unnecessary unbox/box
        } else {
            return val.floatValue();
        }
    }

    public Boolean getBoolean(String key, Boolean def) {
        Objects.requireNonNull(key);
        Object val = map.get(key);
        return val != null || map.containsKey(key) ? (Boolean) val : def;
    }

    @Override
    public JsonObject getJsonObject(String key, JsonObject def) {
        JsonObjectImpl val = getJsonObject(key);
        return val != null || map.containsKey(key) ? val : def;
    }

    @Override
    public JsonArray getJsonArray(String key, JsonArray def) {
        JsonArray val = getJsonArray(key);
        return val != null || map.containsKey(key) ? val : def;
    }

    public byte[] getBinary(String key, byte[] def) {
        Objects.requireNonNull(key);
        Object val = map.get(key);
        return val != null || map.containsKey(key) ? (val == null ? null : Base64.getDecoder().decode((String) val)) : def;
    }

    public Instant getInstant(String key, Instant def) {
        Objects.requireNonNull(key);
        Object val = map.get(key);
        return val != null || map.containsKey(key) ?
                (val == null ? null : Instant.from(ISO_INSTANT.parse((String) val))) : def;
    }

    public Object getValue(String key, Object def) {
        Objects.requireNonNull(key);
        Object val = getValue(key);
        return val != null || map.containsKey(key) ? val : def;
    }

    public boolean containsKey(String key) {
        Objects.requireNonNull(key);
        return map.containsKey(key);
    }

    public Set<String> fieldNames() {
        return map.keySet();
    }

    public JsonObjectImpl put(String key, Enum value) {
        Objects.requireNonNull(key);
        map.put(key, value == null ? null : value.name());
        return this;
    }

    public JsonObjectImpl put(String key, CharSequence value) {
        Objects.requireNonNull(key);
        map.put(key, value == null ? null : value.toString());
        return this;
    }

    public JsonObjectImpl put(String key, String value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    public JsonObjectImpl put(String key, Integer value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    public JsonObjectImpl put(String key, Long value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    public JsonObjectImpl put(String key, Double value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    public JsonObjectImpl put(String key, Float value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    public JsonObjectImpl put(String key, Boolean value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    public JsonObjectImpl putNull(String key) {
        Objects.requireNonNull(key);
        map.put(key, null);
        return this;
    }

    @Override
    public JsonObjectImpl put(String key, JsonObject value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    public JsonObjectImpl put(String key, JsonArray value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    public JsonObjectImpl put(String key, byte[] value) {
        Objects.requireNonNull(key);
        map.put(key, value == null ? null : Base64.getEncoder().encodeToString(value));
        return this;
    }

    public JsonObjectImpl put(String key, Instant value) {
        Objects.requireNonNull(key);
        map.put(key, value == null ? null : ISO_INSTANT.format(value));
        return this;
    }

    public JsonObjectImpl put(String key, Joable value) {
        Objects.requireNonNull(key);
        JsonObject jo = new JsonObjectImpl();
        value.writeTo(jo);
        map.put(key, jo);
        return this;
    }

    @Override
    public JsonObject put(String key, Object value) {
        Objects.requireNonNull(key);
        value = Json.checkAndCopy(value, false);
        map.put(key, value);
        return this;
    }

    public Object remove(String key) {
        return map.remove(key);
    }

    @Override
    public JsonObjectImpl mergeIn(JsonObject other) {
        return mergeIn(other, false);
    }

    @Override
    public JsonObjectImpl mergeIn(JsonObject other, boolean deep) {
        return mergeIn(other, deep ? Integer.MAX_VALUE : 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonObjectImpl mergeIn(JsonObject other, int depth) {
        if (depth < 1) {
            return this;
        }
        if (depth == 1) {
            map.putAll(((JsonObjectImpl) other).map);
            return this;
        }
        for (Map.Entry<String, Object> e : ((JsonObjectImpl) other).map.entrySet()) {
            map.merge(e.getKey(), e.getValue(), (oldVal, newVal) -> {
                if (oldVal instanceof Map) {
                    oldVal = new JsonObjectImpl((Map) oldVal);
                }
                if (newVal instanceof Map) {
                    newVal = new JsonObjectImpl((Map) newVal);
                }
                if (oldVal instanceof JsonObjectImpl && newVal instanceof JsonObjectImpl) {
                    return ((JsonObjectImpl) oldVal).mergeIn((JsonObjectImpl) newVal, depth - 1);
                }
                return newVal;
            });
        }
        return this;
    }

    public String encode() {
        return Json.encode(map);
    }

    public void encode(OutputStream os) {
        Json.encode(os, map);
    }

    public String encodePrettily() {
        return Json.encodePrettily(map);
    }

    public JsonObjectImpl copy() {
        Map<String, Object> copiedMap;
        if (map instanceof LinkedHashMap) {
            copiedMap = new LinkedHashMap<>(map.size());
        } else {
            copiedMap = new HashMap<>(map.size());
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object val = entry.getValue();
            val = Json.checkAndCopy(val, true);
            copiedMap.put(entry.getKey(), val);
        }
        return new JsonObjectImpl(copiedMap);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public Stream<Map.Entry<String, Object>> stream() {
        return Json.asStream(iterator());
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return new Iter(map.entrySet().iterator());
    }

    public int size() {
        return map.size();
    }

    public JsonObjectImpl clear() {
        map.clear();
        return this;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public String toString() {
        return encode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return objectEquals(map, o);
    }

    static boolean objectEquals(Map<?, ?> m1, Object o2) {
        Map<?, ?> m2;
        if (o2 instanceof JsonObjectImpl) {
            m2 = ((JsonObjectImpl) o2).map;
        } else if (o2 instanceof Map<?, ?>) {
            m2 = (Map<?, ?>) o2;
        } else {
            return false;
        }
        if (m1.size() != m2.size())
            return false;
        for (Map.Entry<?, ?> entry : m1.entrySet()) {
            Object val = entry.getValue();
            if (val == null) {
                if (m2.get(entry.getKey()) != null) {
                    return false;
                }
            } else {
                if (!equals(entry.getValue(), m2.get(entry.getKey()))) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean equals(Object o1, Object o2) {
        if (o1 == o2)
            return true;
        if (o1 instanceof JsonObjectImpl) {
            return objectEquals(((JsonObjectImpl) o1).map, o2);
        }
        if (o1 instanceof Map<?, ?>) {
            return objectEquals((Map<?, ?>) o1, o2);
        }
        if (o1 instanceof JsonArrayImpl) {
            return JsonArrayImpl.arrayEquals(((JsonArrayImpl) o1).getList(), o2);
        }
        if (o1 instanceof List<?>) {
            return JsonArrayImpl.arrayEquals((List<?>) o1, o2);
        }
        if (o1 instanceof Number && o2 instanceof Number && o1.getClass() != o2.getClass()) {
            Number n1 = (Number) o1;
            Number n2 = (Number) o2;
            if (o1 instanceof Float || o1 instanceof Double || o2 instanceof Float || o2 instanceof Double) {
                return n1.doubleValue() == n2.doubleValue();
            } else {
                return n1.longValue() == n2.longValue();
            }
        }
        return o1.equals(o2);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    private void fromJson(String json) {
        map = Json.decodeValue(json, Map.class);
    }

    private void fromJson(InputStream is) {
        map = Json.decodeValue(is, Map.class);
    }

    /**
     * 获取字符串参数，不能为空
     *
     * @param key
     * @return
     */
    public String getStringParam(String key) {
        String param = null;
        try {
            param = getString(key);
        } catch (Exception ignore) {
        }
        if (param == null) {
            throw ExceptionDefine.PARAM_ERR.build();
        }

        return param;
    }

    /**
     * 获取int参数，不能为空
     *
     * @param key
     * @return
     */
    public int getIntegerParam(String key) {
        Integer param = null;
        try {
            param = getInteger(key);
        } catch (Exception ignore) {
        }
        if (param == null) {
            throw ExceptionDefine.PARAM_ERR.build();
        }

        return param;
    }

    public static JsonObject wrap(Object val) {
        Objects.requireNonNull(val);
        Obj
    }

    private class Iter implements Iterator<Map.Entry<String, Object>> {

        final Iterator<Map.Entry<String, Object>> mapIter;

        Iter(Iterator<Map.Entry<String, Object>> mapIter) {
            this.mapIter = mapIter;
        }

        @Override
        public boolean hasNext() {
            return mapIter.hasNext();
        }

        @Override
        public Map.Entry<String, Object> next() {
            Map.Entry<String, Object> entry = mapIter.next();
            if (entry.getValue() instanceof Map) {
                return new Entry(entry.getKey(), new JsonObjectImpl((Map) entry.getValue()));
            } else if (entry.getValue() instanceof List) {
                return new Entry(entry.getKey(), new JsonArrayImpl((List) entry.getValue()));
            }
            return entry;
        }

        @Override
        public void remove() {
            mapIter.remove();
        }
    }

    private static final class Entry implements Map.Entry<String, Object> {
        final String key;
        final Object value;

        public Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
    }
}

