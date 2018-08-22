package corex.core.rpc;

import corex.core.JoHolder;
import corex.core.annotation.Param;
import corex.core.exception.CoreException;
import corex.core.json.JsonObject;
import corex.core.rpc.MethodParamDetail.ParamDetail;
import corex.core.rpc.MethodParamDetail.ParamType;
import io.netty.util.internal.StringUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static corex.core.rpc.MethodParamDetail.ParamType.*;

/**
 * Created by Joshua on 2018/3/8.
 */
public class ParamParser {

    public MethodParamDetail parseMethodParamDetail(Method method) {

        ParamDetail[] paramDetails = new ParamDetail[method.getParameterCount()];

        boolean isVoidType;

        int i = 0;
        for (Parameter p : method.getParameters()) {
            Param param = p.getAnnotation(Param.class);
            if (param == null) {
                throw new NullPointerException(method.getDeclaringClass().getSimpleName() + "." + method.getName() + "方法第" + (i + 1) + "个参数没有Param注解");
            }

            if (StringUtil.isNullOrEmpty(param.value())) {
                throw new CoreException("类型不能为空");
            }

            Class<?> clz = p.getType();
            ParamType type = UNSUPPORTED;
            ParamType parameterizedType = UNSUPPORTED;
            if (clz == boolean.class || clz == Boolean.class) {
                type = BOOLEAN;
            } else if (clz == int.class || clz == Integer.class) {
                type = INT;
            } else if (clz == long.class || clz == Long.class) {
                type = LONG;
            } else if (clz == double.class || clz == Double.class) {
                type = DOUBLE;
            } else if (clz == String.class) {
                type = STRING;
            } else if (clz == JsonObject.class) {
                type = JO;
            } else if (clz == List.class) {
                type = LIST;
                parameterizedType = parseListParameterizedType(p);
            } else if (clz.isArray()) {
//                type = ARRAY;
            }

            if (type == UNSUPPORTED) {
                throw new CoreException("不支持的参数类型:" + clz.getName());
            }
            paramDetails[i++] = new ParamDetail(param, type, parameterizedType);
        }

        Class<?> clz = method.getReturnType();
        if (clz == Void.class || clz == void.class) {
            isVoidType = true;
        } else if (clz == JoHolder.class) {
            isVoidType = false;
        } else {
            throw new CoreException("不正确的返回参数类型:" + clz.getName());
        }

        return new MethodParamDetail(method, paramDetails, isVoidType);
    }

    private static ParamType parseListParameterizedType(Parameter parameter) {
        Type type = parameter.getParameterizedType();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] ts = parameterizedType.getActualTypeArguments();
            if (ts.length == 1) {
                if (ts[0] instanceof Class) {
                    return parseParameterizedType((Class) ts[0]);
                }
            }
        }
        throw new CoreException("List<T>参数错误");
    }

    private static ParamType parseParameterizedType(Class<?> clz) {
        ParamType type = UNSUPPORTED;
        if (clz == Boolean.class) {
            type = BOOLEAN;
        } else if (clz == Integer.class) {
            type = INT;
        } else if (clz == Long.class) {
            type = LONG;
        } else if (clz == Double.class) {
            type = DOUBLE;
        } else if (clz == String.class) {
            type = STRING;
        } else if (clz == JsonObject.class) {
            type = JO;
        }

        if (type == UNSUPPORTED) {
            throw new CoreException("不支持的泛型参数类型:" + clz.getName());
        }

        return type;
    }

}
