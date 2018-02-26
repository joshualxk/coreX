package corex.core;

import corex.core.impl.MoImpl;
import corex.proto.ModelProto;

/**
 * Created by Joshua on 2018/3/14.
 * 类似json
 */
public interface Mo {

    static Mo mo() {
        return new MoImpl();
    }

    void putBoolean(String k, boolean v);

    void putInt(String k, int v);

    void putLong(String k, long v);

    void putDouble(String k, double v);

    void putString(String k, String v);

    void putList(String k, Lo list);

    void putMo(String k, Mo mo);

    boolean getBoolean(String k);

    int getInt(String k);

    long getLong(String k);

    double getDouble(String k);

    String getString(String k);

    Lo getList(String k);

    Mo getMo(String k);

    boolean getBooleanOrDefault(String k, boolean defVal);

    int getIntOrDefault(String k, int defVal);

    long getLongOrDefault(String k, long defVal);

    double getDoubleOrDefault(String k, double defVal);

    String getStringOrDefault(String k, String defVal);

    Mo getMoOrDefault(String k, Mo defVal);

    boolean containsKey(String k);

    ModelProto.Struct.Builder builder();

    ModelProto.BodyHolder toBodyHolder();

    int size();

}
