package io.bigoldbro.corex.rpc;

import com.google.protobuf.GeneratedMessageV3;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Joshua on 2018/3/15.
 */
public class MethodDetail {

    public final Method method;
    public final List<ParamDetail> params;
    public final ReturnDetail returnDetail;

    MethodDetail(Method method, List<ParamDetail> params, ReturnDetail returnDetail) {
        this.method = method;
        this.params = params;
        this.returnDetail = returnDetail;
    }

    public String name() {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    public static class ParamDetail {
        public final String name;
        public final boolean optional;
        public final ParamType type;
        public final ParamType parameterizedType;
        public final Class<? extends GeneratedMessageV3> msgClz;

        public ParamDetail(String name, boolean optional, ParamType type, ParamType parameterizedType, Class<? extends GeneratedMessageV3> msgClz) {
            this.name = name;
            this.optional = optional;
            this.type = type;
            this.parameterizedType = parameterizedType;
            this.msgClz = msgClz;
        }
    }
}
