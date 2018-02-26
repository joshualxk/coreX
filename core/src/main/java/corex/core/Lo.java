package corex.core;

import corex.core.impl.LoImpl;
import corex.proto.ModelProto;

import java.util.List;

/**
 * Created by Joshua on 2018/3/14.
 * 类似json array
 */
public interface Lo {

    static Lo lo() {
        return new LoImpl();
    }

    List<Boolean> getBooleanList();

    List<Integer> getIntList();

    List<Long> getLongList();

    List<Double> getDoubleList();

    List<String> getStringList();

    List<Mo> getMoList();

    void setBooleanList(List<Boolean> list);

    void setIntList(List<Integer> list);

    void setLongList(List<Long> list);

    void setDoubleList(List<Double> list);

    void setStringList(List<String> list);

    void setMoList(List<Mo> list);

    void addString(String v);

    void addMo(Mo v);

    int size();

    ModelProto.ListValue.Builder builder();
}
