package io.bigoldbro.corex.json;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

/**
 * Created by Joshua on 2018/8/23.
 */
public class JsonArrayImpl implements JsonArray {

    private List<Object> list;

    public JsonArrayImpl(String json) {
        fromJson(json);
    }

    public JsonArrayImpl() {
        list = new ArrayList<>();
    }

    public JsonArrayImpl(List list) {
        this.list = list;
    }

    public String getString(int pos) {
        CharSequence cs = (CharSequence) list.get(pos);
        return cs == null ? null : cs.toString();
    }

    public Integer getInteger(int pos) {
        Number number = (Number) list.get(pos);
        if (number == null) {
            return null;
        } else if (number instanceof Integer) {
            return (Integer) number; // Avoids unnecessary unbox/box
        } else {
            return number.intValue();
        }
    }

    public Long getLong(int pos) {
        Number number = (Number) list.get(pos);
        if (number == null) {
            return null;
        } else if (number instanceof Long) {
            return (Long) number; // Avoids unnecessary unbox/box
        } else {
            return number.longValue();
        }
    }

    public Double getDouble(int pos) {
        Number number = (Number) list.get(pos);
        if (number == null) {
            return null;
        } else if (number instanceof Double) {
            return (Double) number; // Avoids unnecessary unbox/box
        } else {
            return number.doubleValue();
        }
    }

    public Float getFloat(int pos) {
        Number number = (Number) list.get(pos);
        if (number == null) {
            return null;
        } else if (number instanceof Float) {
            return (Float) number; // Avoids unnecessary unbox/box
        } else {
            return number.floatValue();
        }
    }

    public Boolean getBoolean(int pos) {
        return (Boolean) list.get(pos);
    }

    public JsonObjectImpl getJsonObject(int pos) {
        Object val = list.get(pos);
        if (val instanceof Map) {
            val = new JsonObjectImpl((Map) val);
        }
        return (JsonObjectImpl) val;
    }

    public JsonArrayImpl getJsonArray(int pos) {
        Object val = list.get(pos);
        if (val instanceof List) {
            val = new JsonArrayImpl((List) val);
        }
        return (JsonArrayImpl) val;
    }

    public byte[] getBinary(int pos) {
        String val = (String) list.get(pos);
        if (val == null) {
            return null;
        } else {
            return Base64.getDecoder().decode(val);
        }
    }

    public Instant getInstant(int pos) {
        String val = (String) list.get(pos);
        if (val == null) {
            return null;
        } else {
            return Instant.from(ISO_INSTANT.parse(val));
        }
    }

    public Object getValue(int pos) {
        Object val = list.get(pos);
        if (val instanceof Map) {
            val = new JsonObjectImpl((Map) val);
        } else if (val instanceof List) {
            val = new JsonArrayImpl((List) val);
        }
        return val;
    }

    public boolean hasNull(int pos) {
        return list.get(pos) == null;
    }

    public JsonArrayImpl add(Enum value) {
        Objects.requireNonNull(value);
        list.add(value.name());
        return this;
    }

    public JsonArrayImpl add(CharSequence value) {
        Objects.requireNonNull(value);
        list.add(value.toString());
        return this;
    }

    public JsonArrayImpl add(String value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    public JsonArrayImpl add(Integer value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    public JsonArrayImpl add(Long value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    public JsonArrayImpl add(Double value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    public JsonArrayImpl add(Float value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    public JsonArrayImpl add(Boolean value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    public JsonArrayImpl addNull() {
        list.add(null);
        return this;
    }

    public JsonArrayImpl add(JsonObject value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    public JsonArrayImpl add(JsonArray value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    public JsonArrayImpl add(byte[] value) {
        Objects.requireNonNull(value);
        list.add(Base64.getEncoder().encodeToString(value));
        return this;
    }

    public JsonArrayImpl add(Instant value) {
        Objects.requireNonNull(value);
        list.add(ISO_INSTANT.format(value));
        return this;
    }

    public JsonArrayImpl add(Joable value) {
        Objects.requireNonNull(value);
        list.add(Json.toJsonObject(value));
        return this;
    }

    public JsonArrayImpl add(Object value) {
        Objects.requireNonNull(value);
        value = Json.checkAndCopy(value, false);
        list.add(value);
        return this;
    }

    public JsonArrayImpl addAll(JsonArray array) {
        Objects.requireNonNull(array);
        list.addAll(((JsonArrayImpl) array).list);
        return this;
    }

    public boolean contains(Object value) {
        return list.contains(value);
    }

    public boolean remove(Object value) {
        return list.remove(value);
    }

    public Object remove(int pos) {
        Object removed = list.remove(pos);
        if (removed instanceof Map) {
            return new JsonObjectImpl((Map) removed);
        } else if (removed instanceof ArrayList) {
            return new JsonArrayImpl((List) removed);
        }
        return removed;
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public List getList() {
        return list;
    }

    public JsonArrayImpl clear() {
        list.clear();
        return this;
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iter(list.iterator());
    }

    public String encode() {
        return Json.encode(list);
    }

    public String encodePrettily() {
        return Json.encodePrettily(list);
    }

    public JsonArrayImpl copy() {
        List<Object> copiedList = new ArrayList<>(list.size());
        for (Object val : list) {
            val = Json.checkAndCopy(val, true);
            copiedList.add(val);
        }
        return new JsonArrayImpl(copiedList);
    }

    public Stream<Object> stream() {
        return Json.asStream(iterator());
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
        return arrayEquals(list, o);
    }

    static boolean arrayEquals(List<?> l1, Object o2) {
        List<?> l2;
        if (o2 instanceof JsonArrayImpl) {
            l2 = ((JsonArrayImpl) o2).list;
        } else if (o2 instanceof List<?>) {
            l2 = (List<?>) o2;
        } else {
            return false;
        }
        if (l1.size() != l2.size())
            return false;
        Iterator<?> iter = l2.iterator();
        for (Object entry : l1) {
            Object other = iter.next();
            if (entry == null) {
                if (other != null) {
                    return false;
                }
            } else if (!JsonObjectImpl.equals(entry, other)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    private void fromJson(String json) {
        list = Json.decodeValue(json, List.class);
    }

    private class Iter implements Iterator<Object> {

        final Iterator<Object> listIter;

        Iter(Iterator<Object> listIter) {
            this.listIter = listIter;
        }

        @Override
        public boolean hasNext() {
            return listIter.hasNext();
        }

        @Override
        public Object next() {
            Object val = listIter.next();
            if (val instanceof Map) {
                val = new JsonObjectImpl((Map) val);
            } else if (val instanceof List) {
                val = new JsonArrayImpl((List) val);
            }
            return val;
        }

        @Override
        public void remove() {
            listIter.remove();
        }
    }


}
