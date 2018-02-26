package corex.core.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import corex.core.Lo;
import corex.core.Mo;
import corex.core.define.ConstDefine;
import corex.core.exception.CoreException;
import corex.proto.ModelProto.BodyHolder;
import corex.proto.ModelProto.Struct;
import corex.proto.ModelProto.Value;
import io.netty.util.internal.StringUtil;

import java.util.Objects;

/**
 * Created by Joshua on 2018/3/14.
 */
public class ReadOnlyFutureMo extends DummyFutureMo {

    private final Struct struct;

    public ReadOnlyFutureMo(BodyHolder bodyHolder) {
        Objects.requireNonNull(bodyHolder, "bodyHolder");
        if (bodyHolder.getType() != ConstDefine.BODY_TYPE_STRUCT) {
            throw new CoreException("Unsupported bodyHolder type：" + bodyHolder.getType());
        }
        try {
            struct = Struct.parseFrom(bodyHolder.getBody());
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("bodyHolder解析错误");
        }
    }

    ReadOnlyFutureMo(Struct struct) {
        this.struct = struct;
    }

    @Override
    public boolean getBoolean(String k) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.BOOL_VALUE) {
            return v.getBoolValue();
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public int getInt(String k) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.INT_VALUE) {
            return v.getIntValue();
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public long getLong(String k) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.LONG_VALUE) {
            return v.getLongValue();
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public double getDouble(String k) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.DOUBLE_VALUE) {
            return v.getDoubleValue();
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public String getString(String k) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.STRING_VALUE) {
            return v.getStringValue();
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public Lo getList(String k) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.hasListValue()) {
            return new ReadOnlyLo(v.getListValue());
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public Mo getMo(String k) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.hasStructValue()) {
            return new ReadOnlyFutureMo(v.getStructValue());
        }
        throw new IllegalArgumentException(k);
    }

    @Override
    public boolean getBooleanOrDefault(String k, boolean defVal) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.BOOL_VALUE) {
            return v.getBoolValue();
        }
        return defVal;
    }

    @Override
    public int getIntOrDefault(String k, int defVal) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.INT_VALUE) {
            return v.getIntValue();
        }
        return defVal;
    }

    @Override
    public long getLongOrDefault(String k, long defVal) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.LONG_VALUE) {
            return v.getLongValue();
        }
        return defVal;
    }

    @Override
    public double getDoubleOrDefault(String k, double defVal) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.DOUBLE_VALUE) {
            return v.getDoubleValue();
        }
        return defVal;
    }

    @Override
    public String getStringOrDefault(String k, String defVal) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.STRING_VALUE) {
            String s = v.getStringValue();
            if (!StringUtil.isNullOrEmpty(s)) {
                return s;
            }
        }
        return defVal;
    }

    @Override
    public Mo getMoOrDefault(String k, Mo defVal) {
        Value v = struct.getFieldsOrThrow(k);
        if (v.getKindCase() == Value.KindCase.STRUCT_VALUE) {
            return new ReadOnlyFutureMo(v.getStructValue());
        }
        return defVal;
    }

    @Override
    public boolean containsKey(String k) {
        return struct.containsFields(k);
    }

    @Override
    public int size() {
        return struct.getFieldsCount();
    }

    @Override
    public String toString() {
        return "Mo:[" + struct.toString() + "]";
    }
}
