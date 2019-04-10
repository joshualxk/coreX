package io.bigoldbro.corex.rpc;

import com.google.protobuf.GeneratedMessageV3;
import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.rpc.MethodDetail.ParamDetail;
import io.netty.util.internal.StringUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static io.bigoldbro.corex.rpc.ParamType.*;

/**
 * Created by Joshua on 2018/3/8.
 */
public class ParamParser {

    public static MethodDetail parseMethodDetail(Method method) {

        List<ParamDetail> list = new ArrayList<>(method.getParameterCount());

        Set<String> argCheck = new HashSet<>(method.getParameterCount());

        int i = 0;
        for (Parameter p : method.getParameters()) {
            Param param = p.getAnnotation(Param.class);

            String argName;
            boolean optional;
            if (param == null) {
                argName = defaultArgName(i);
                optional = false;
            } else {
                argName = StringUtil.isNullOrEmpty(param.value()) ? defaultArgName(i) : param.value();
                optional = param.optional();
            }

            if (!argCheck.add(argName)) {
                throw new CoreException("重复的参数名:" + method.getName() + ":" + argName);
            }

            Class<?> clz = p.getType();

            ParamType type = UNSUPPORTED;
            ParamType parameterizedType = UNSUPPORTED;
            Class<? extends GeneratedMessageV3> msgClz = null;

            if (clz == boolean.class || clz == Boolean.class) {
                type = BOOLEAN;
            } else if (clz == int.class || clz == Integer.class) {
                type = INT;
            } else if (clz == long.class || clz == Long.class) {
                type = LONG;
            } else if (clz == String.class) {
                type = STRING;
            } else if (clz == double.class || clz == Double.class) {
                type = DOUBLE;
            } else if (clz.isAssignableFrom(GeneratedMessageV3.class)) {
                type = PROTO;
                msgClz = (Class<? extends GeneratedMessageV3>) clz;
            } else if (clz == List.class) {
                type = LIST;
                Class parameterizedClz = resolveClass(p.getParameterizedType());
                parameterizedType = parseParamType(parameterizedClz);

                if (parameterizedType == PROTO) {
                    msgClz = parameterizedClz;
                }
            } else if (clz.isArray()) {
                type = ARRAY;
                Class componentClz = clz.getComponentType();
                parameterizedType = parseParamType(componentClz);

                if (componentClz.isPrimitive()) {
                    type = RAW_ARRAY;
                } else if (parameterizedType == PROTO) {
                    msgClz = componentClz;
                }
            }

            if (type == UNSUPPORTED) {
                throw new CoreException("不支持的参数类型:" + clz.getName());
            }

            list.add(new ParamDetail(argName, optional, type, parameterizedType, msgClz));
        }

        ReturnDetail returnDetail;

        Type returnType = method.getGenericReturnType();
        if (returnType == Void.class || returnType == void.class) {
            returnDetail = new ReturnDetail(true, UNSUPPORTED, null);
        } else if (returnType == Callback.class) {
            Class<?> returnClz = resolveClass(returnType);
            ParamType paramType = parseParamType(returnClz);
            returnDetail = new ReturnDetail(false, paramType, paramType == JOABLE ? returnClz : null);
        } else {
            throw new CoreException("不正确的返回参数类型:" + returnType.getTypeName());
        }

        return new MethodDetail(method, Collections.unmodifiableList(list), returnDetail);
    }

    private static String defaultArgName(int n) {
        return "arg" + n;
    }

    private static Class<?> resolveClass(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] ts = parameterizedType.getActualTypeArguments();
            if (ts.length == 1) {
                if (ts[0] instanceof Class) {
                    return (Class) ts[0];
                }
            }
        }
        throw new CoreException("<T>参数错误");
    }

    private static ParamType parseParamType(Class<?> clz) {
        ParamType type = UNSUPPORTED;
        if (clz == Boolean.class || clz == boolean.class) {
            type = BOOLEAN;
        } else if (clz == Byte.class || clz == byte.class) {
            type = BYTE;
        } else if (clz == Short.class || clz == short.class) {
            type = SHORT;
        } else if (clz == Integer.class || clz == int.class) {
            type = INT;
        } else if (clz == Long.class || clz == long.class) {
            type = LONG;
        } else if (clz == Float.class || clz == float.class) {
            type = FLOAT;
        } else if (clz == Double.class || clz == double.class) {
            type = DOUBLE;
        } else if (clz == String.class) {
            type = STRING;
        } else if (clz.isAssignableFrom(GeneratedMessageV3.class)) {
            type = PROTO;
        }

        if (type == UNSUPPORTED) {
            throw new CoreException("不支持的泛型参数类型:" + clz.getName());
        }

        return type;
    }

}
