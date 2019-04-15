package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.NetData;
import io.bigoldbro.corex.exception.CoreException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.bigoldbro.corex.rpc.ParamType.*;

/**
 * Created by Joshua on 2018/3/8.
 */
public class ModuleParser {

    // TODO 优化，支持更多类型
    @SuppressWarnings("unchecked")
    static MethodDetail parseMethodDetail(Method method) {

        List<ParamDetail> list = new ArrayList<>(method.getParameterCount());

        for (Parameter p : method.getParameters()) {

            Type type = p.getParameterizedType();
            ParamDetail paramDetail = parseParamDetail(type);

            list.add(paramDetail);
        }

        ReturnDetail returnDetail;

        {
            Class<?> clz = method.getReturnType();
            if (clz == Void.class || clz == void.class) {
                returnDetail = new ReturnDetail(UNSUPPORTED, UNSUPPORTED, null, true, false);
            } else {
                Type type = method.getGenericReturnType();

                boolean isCallback = false;
                if (clz == Future.class) {
                    isCallback = true;
                    type = resolveType(type, 0);
                }

                ParamDetail paramDetail = parseParamDetail(type);

                returnDetail = new ReturnDetail(paramDetail.type, paramDetail.genericType, paramDetail.extClz, false, isCallback);
            }
        }

        return new MethodDetail(method, Collections.unmodifiableList(list), returnDetail);
    }

    private static ParamDetail parseParamDetail(Type type) {
        Class<?> clz = resolveClass(type);

        ParamType pt = UNSUPPORTED;
        ParamType ppt = UNSUPPORTED;
        Class<?> extClz = clz;

        if (clz == boolean.class || clz == Boolean.class) {
            pt = BOOLEAN;
        } else if (clz == byte.class || clz == Byte.class) {
            pt = BYTE;
        } else if (clz == short.class || clz == Short.class) {
            pt = SHORT;
        } else if (clz == int.class || clz == Integer.class) {
            pt = INT;
        } else if (clz == long.class || clz == Long.class) {
            pt = LONG;
        } else if (clz == float.class || clz == Float.class) {
            pt = FLOAT;
        } else if (clz == double.class || clz == Double.class) {
            pt = DOUBLE;
        } else if (clz == String.class) {
            pt = STRING;
        } else if (NetData.class.isAssignableFrom(clz)) {
            pt = NET_DATA;
        } else if (clz == List.class) {
            pt = LIST;
            Class parameterizedClz = resolveClass(resolveType(type, 0));
            ppt = parseParamType(parameterizedClz);

            extClz = parameterizedClz;
        } else if (clz == Map.class) {
            pt = MAP;

            if (resolveClass(resolveType(type, 0)) != String.class) {
                throw new CoreException("不支持的Map.k类型");
            }

            Class parameterizedClz = resolveClass(resolveType(type, 1));
            ppt = parseParamType(parameterizedClz);

            extClz = parameterizedClz;
        } else if (clz.isArray()) {
            pt = ARRAY;
            Class componentClz = clz.getComponentType();
            ppt = parseParamType(componentClz);

            extClz = componentClz;
        }

        if (pt == UNSUPPORTED) {
            throw new CoreException("不支持的参数类型:" + clz.getName());
        }

        return new ParamDetail(pt, ppt, extClz);
    }

    private static Type resolveType(Type type, int index) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] ts = parameterizedType.getActualTypeArguments();
            return ts[index];
        }
        throw new CoreException("泛型参数错误:" + type.getTypeName());
    }

    private static Class<?> resolveClass(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class<?>) parameterizedType.getRawType();
        } else if (type instanceof Class) {
            return (Class<?>) type;
        }
        throw new CoreException("泛型参数错误:" + type.getTypeName());
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
        } else if (NetData.class.isAssignableFrom(clz)) {
            type = NET_DATA;
        }

        if (type == UNSUPPORTED) {
            throw new CoreException("不支持的泛型参数类型:" + clz.getName());
        }

        return type;
    }

}
