package corex.core.impl;

import corex.core.*;
import corex.proto.ModelProto;

/**
 * Created by Joshua on 2018/3/15.
 */
public class DummyFutureMo implements FutureMo {

    @Override
    public void addListener(Handler<AsyncResult<Mo>> handler) {
        throw new UnsupportedOperationException("addListener");
    }

    @Override
    public ModelProto.BodyHolder toBodyHolder() {
        throw new UnsupportedOperationException("toBodyHolder");
    }

    @Override
    public void putBoolean(String k, boolean v) {
        throw new UnsupportedOperationException("putBoolean");
    }

    @Override
    public void putInt(String k, int v) {
        throw new UnsupportedOperationException("putInt");
    }

    @Override
    public void putLong(String k, long v) {
        throw new UnsupportedOperationException("putLong");
    }

    @Override
    public void putDouble(String k, double v) {
        throw new UnsupportedOperationException("putDouble");
    }

    @Override
    public void putString(String k, String v) {
        throw new UnsupportedOperationException("putString");
    }

    @Override
    public void putList(String k, Lo list) {
        throw new UnsupportedOperationException("putList");
    }

    @Override
    public void putMo(String k, Mo map) {
        throw new UnsupportedOperationException("putMo");
    }

    @Override
    public boolean getBoolean(String k) {
        throw new UnsupportedOperationException("getBoolean");
    }

    @Override
    public int getInt(String k) {
        throw new UnsupportedOperationException("getInt");
    }

    @Override
    public long getLong(String k) {
        throw new UnsupportedOperationException("getLong");
    }

    @Override
    public double getDouble(String k) {
        throw new UnsupportedOperationException("getDouble");
    }

    @Override
    public String getString(String k) {
        throw new UnsupportedOperationException("getString");
    }

    @Override
    public Lo getList(String k) {
        throw new UnsupportedOperationException("getList");
    }

    @Override
    public Mo getMo(String k) {
        throw new UnsupportedOperationException("getMo");
    }

    @Override
    public boolean getBooleanOrDefault(String k, boolean defVal) {
        return defVal;
    }

    @Override
    public int getIntOrDefault(String k, int defVal) {
        return defVal;
    }

    @Override
    public long getLongOrDefault(String k, long defVal) {
        return defVal;
    }

    @Override
    public double getDoubleOrDefault(String k, double defVal) {
        return defVal;
    }

    @Override
    public String getStringOrDefault(String k, String defVal) {
        return defVal;
    }

    @Override
    public Mo getMoOrDefault(String k, Mo defVal) {
        return defVal;
    }

    @Override
    public boolean containsKey(String k) {
        return false;
    }

    @Override
    public ModelProto.Struct.Builder builder() {
        throw new UnsupportedOperationException("builder");
    }

    @Override
    public int size() {
        return 0;
    }

}
