package corex.core.impl;

import corex.core.Lo;
import corex.core.Mo;
import corex.core.define.ConstDefine;
import corex.proto.ModelProto;
import corex.proto.ModelProto.Struct.Builder;
import corex.proto.ModelProto.Value;

/**
 * Created by Joshua on 2018/3/14.
 */
public class MoImpl implements Mo {

    final Builder structBuilder;

    public MoImpl() {
        structBuilder = ModelProto.Struct.newBuilder();
    }

    MoImpl(ModelProto.Struct struct) {
        structBuilder = struct.toBuilder();
    }

    @Override
    public void putBoolean(String k, boolean v) {
        structBuilder.putFields(k, Value.newBuilder().setBoolValue(v).build());
    }

    @Override
    public void putInt(String k, int v) {
        structBuilder.putFields(k, Value.newBuilder().setIntValue(v).build());
    }

    @Override
    public void putLong(String k, long v) {
        structBuilder.putFields(k, Value.newBuilder().setLongValue(v).build());
    }

    @Override
    public void putDouble(String k, double v) {
        structBuilder.putFields(k, Value.newBuilder().setDoubleValue(v).build());
    }

    @Override
    public void putString(String k, String v) {
        if (v == null) {
            return;
        }
        structBuilder.putFields(k, Value.newBuilder().setStringValue(v).build());
    }

    @Override
    public void putList(String k, Lo list) {
        if (list == null) {
            return;
        }
        structBuilder.putFields(k, Value.newBuilder().setListValue(list.builder()).build());
    }

    @Override
    public void putMo(String k, Mo map) {
        if (map == null) {
            return;
        }
        structBuilder.putFields(k, Value.newBuilder().setStructValue(map.builder()).build());
    }

    @Override
    public boolean getBoolean(String k) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.BOOL_VALUE) {
            return v.getBoolValue();
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public int getInt(String k) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.INT_VALUE) {
            return v.getIntValue();
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public long getLong(String k) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.LONG_VALUE) {
            return v.getLongValue();
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public double getDouble(String k) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.DOUBLE_VALUE) {
            return v.getDoubleValue();
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public String getString(String k) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.STRING_VALUE) {
            return v.getStringValue();
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public Lo getList(String k) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.hasListValue()) {
            return new LoImpl(v.getListValue());
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public Mo getMo(String k) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.hasStructValue()) {
            return new MoImpl(v.getStructValue());
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public boolean getBooleanOrDefault(String k, boolean defVal) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.BOOL_VALUE) {
            return v.getBoolValue();
        }
        return defVal;
    }

    @Override
    public int getIntOrDefault(String k, int defVal) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.INT_VALUE) {
            return v.getIntValue();
        }
        return defVal;
    }

    @Override
    public long getLongOrDefault(String k, long defVal) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.LONG_VALUE) {
            return v.getLongValue();
        }
        return defVal;
    }

    @Override
    public double getDoubleOrDefault(String k, double defVal) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.DOUBLE_VALUE) {
            return v.getDoubleValue();
        }
        return defVal;
    }

    @Override
    public String getStringOrDefault(String k, String defVal) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.STRING_VALUE) {
            return v.getStringValue();
        }
        return defVal;
    }

    @Override
    public Mo getMoOrDefault(String k, Mo defVal) {
        Value v = structBuilder.getFieldsOrThrow(k);
        if (v.hasStructValue()) {
            return new MoImpl(v.getStructValue());
        }
        return defVal;
    }

    @Override
    public boolean containsKey(String k) {
        return structBuilder.containsFields(k);
    }

    @Override
    public Builder builder() {
        return structBuilder;
    }

    @Override
    public int size() {
        return structBuilder.getFieldsCount();
    }

    @Override
    public ModelProto.BodyHolder toBodyHolder() {
        return ModelProto.BodyHolder.newBuilder().setType(ConstDefine.BODY_TYPE_STRUCT).setBody(structBuilder.build().toByteString()).build();
    }

    @Override
    public String toString() {
        return "MoImpl{" +
                "structBuilder=" + structBuilder +
                '}';
    }
}
