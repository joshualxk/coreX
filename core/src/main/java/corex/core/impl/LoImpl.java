package corex.core.impl;

import corex.core.Lo;
import corex.core.Mo;
import corex.proto.ModelProto.ListValue;
import corex.proto.ModelProto.ListValue.Builder;
import corex.proto.ModelProto.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joshua on 2018/3/22.
 */
public class LoImpl implements Lo {

    private final Builder builder;

    public LoImpl() {
        builder = ListValue.newBuilder();
    }

    public LoImpl(ListValue listValue) {
        this.builder = listValue.toBuilder();
    }

    @Override
    public List<Boolean> getBooleanList() {
        List<Value> vList = builder.getValuesList();

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
        List<Value> vList = builder.getValuesList();

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
        List<Value> vList = builder.getValuesList();

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
        List<Value> vList = builder.getValuesList();

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
        List<Value> vList = builder.getValuesList();

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
        List<Value> vList = builder.getValuesList();

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
        builder.clear();
        for (boolean v : list) {
            builder.addValues(Value.newBuilder().setBoolValue(v));
        }
    }

    @Override
    public void setIntList(List<Integer> list) {
        builder.clear();
        for (int v : list) {
            builder.addValues(Value.newBuilder().setIntValue(v));
        }
    }

    @Override
    public void setLongList(List<Long> list) {
        builder.clear();
        for (long v : list) {
            builder.addValues(Value.newBuilder().setLongValue(v));
        }
    }

    @Override
    public void setDoubleList(List<Double> list) {
        builder.clear();
        for (double v : list) {
            builder.addValues(Value.newBuilder().setDoubleValue(v));
        }
    }

    @Override
    public void setStringList(List<String> list) {
        builder.clear();
        for (String v : list) {
            builder.addValues(Value.newBuilder().setStringValue(v));
        }
    }

    @Override
    public void setMoList(List<Mo> list) {
        builder.clear();
        for (Mo mo : list) {
            builder.addValues(Value.newBuilder().setStructValue(mo.builder()));
        }
    }

    @Override
    public void addString(String v) {
        if (v == null) {
            return;
        }
        builder.addValues(Value.newBuilder().setStringValue(v));
    }

    @Override
    public void addMo(Mo v) {
        if (v == null) {
            return;
        }
        builder.addValues(Value.newBuilder().setStructValue(v.builder()));
    }

    @Override
    public int size() {
        return builder.getValuesCount();
    }

    @Override
    public Builder builder() {
        return builder;
    }
}
