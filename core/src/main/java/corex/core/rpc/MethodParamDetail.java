package corex.core.rpc;

import corex.core.annotation.Param;

import java.lang.reflect.Method;

/**
 * Created by Joshua on 2018/3/15.
 */
public class MethodParamDetail {

    public final Method method;
    public final ParamDetail[] params;
    public final boolean isVoidType;

    public MethodParamDetail(Method method, ParamDetail[] params, boolean isVoidType) {
        this.method = method;
        this.params = params;
        this.isVoidType = isVoidType;
    }

    public String name() {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    public static class ParamDetail {
        public final Param param;
        public final ParamType type;
        public final ParamType parameterizedType;

        public ParamDetail(Param param, ParamType type, ParamType parameterizedType) {
            this.param = param;
            this.type = type;
            this.parameterizedType = parameterizedType;
        }
    }

    public enum ParamType {
        UNSUPPORTED,
        BOOLEAN,
        INT,
        LONG,
        DOUBLE,
        STRING,
        ARRAY,
        LIST,
        JO,
    }
}
