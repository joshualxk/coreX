package corex.core.impl;

import corex.core.Lo;
import corex.core.Mo;
import corex.proto.ModelProto.ListValue;
import corex.proto.ModelProto.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joshua on 2018/3/22.
 */
public class ReadOnlyLo implements Lo {

    private final ListValue list;

    public ReadOnlyLo(ListValue list) {
        this.list = list;
    }

    @Override
    public List<Boolean> getBooleanList() {
        List<Value> vList = list.getValuesList();

        List<Boolean> ret = new ArrayList<>(vList.size());
        for (Value v : vList) {
            if (v.getKindCase() != Value.KindCase.BOOL_VALUE) {
                throw new IllegalArgumentException("getBooleanList");
            }
            ret.add(v.getBoolValue());
        }
        return ret;
    }

    @Override
    public List<Integer> getIntList() {
        List<Value> vList = list.getValuesList();

        List<Integer> ret = new ArrayList<>(vList.size());
        for (Value v : vList) {
            if (v.getKindCase() != Value.KindCase.INT_VALUE) {
                throw new IllegalArgumentException("getIntList");
            }
            ret.add(v.getIntValue());
        }
        return ret;
    }

    @Override
    public List<Long> getLongList() {
        List<Value> vList = list.getValuesList();

        List<Long> ret = new ArrayList<>(vList.size());
        for (Value v : vList) {
            if (v.getKindCase() != Value.KindCase.LONG_VALUE) {
                throw new IllegalArgumentException("getLongList");
            }
            ret.add(v.getLongValue());
        }
        return ret;
    }

    @Override
    public List<Double> getDoubleList() {
        List<Value> vList = list.getValuesList();

        List<Double> ret = new ArrayList<>(vList.size());
        for (Value v : vList) {
            if (v.getKindCase() != Value.KindCase.DOUBLE_VALUE) {
                throw new IllegalArgumentException("getDoubleList");
            }
            ret.add(v.getDoubleValue());
        }
        return ret;
    }

    @Override
    public List<String> getStringList() {
        List<Value> vList = list.getValuesList();

        List<String> ret = new ArrayList<>(vList.size());
        for (Value v : vList) {
            if (v.getKindCase() != Value.KindCase.STRING_VALUE) {
                throw new IllegalArgumentException("getStringList");
            }
            ret.add(v.getStringValue());
        }
        return ret;
    }

    @Override
    public List<Mo> getMoList() {
        List<Value> vList = list.getValuesList();

        List<Mo> ret = new ArrayList<>(vList.size());
        for (Value v : vList) {
            if (v.getKindCase() != Value.KindCase.STRUCT_VALUE) {
                throw new IllegalArgumentException("getStringList");
            }
            ret.add(new ReadOnlyFutureMo(v.getStructValue()));
        }
        return ret;
    }

    @Override
    public void setBooleanList(List<Boolean> list) {
        throw new UnsupportedOperationException("setBooleanList");
    }

    @Override
    public void setIntList(List<Integer> list) {
        throw new UnsupportedOperationException("setIntList");
    }

    @Override
    public void setLongList(List<Long> list) {
        throw new UnsupportedOperationException("setLongList");
    }

    @Override
    public void setDoubleList(List<Double> list) {
        throw new UnsupportedOperationException("setDoubleList");
    }

    @Override
    public void setStringList(List<String> list) {
        throw new UnsupportedOperationException("setStringList");
    }

    @Override
    public void setMoList(List<Mo> list) {
        throw new UnsupportedOperationException("setMoList");
    }

    @Override
    public void addString(String v) {
        throw new UnsupportedOperationException("addString");
    }

    @Override
    public void addMo(Mo v) {
        throw new UnsupportedOperationException("addMo");
    }

    @Override
    public int size() {
        return list.getValuesCount();
    }

    @Override
    public ListValue.Builder builder() {
        throw new UnsupportedOperationException("builder");
    }
}
