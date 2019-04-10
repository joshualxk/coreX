package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.json.Json;
import io.bigoldbro.corex.json.JsonArray;
import io.bigoldbro.corex.json.JsonObject;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.model.Auth;
import io.bigoldbro.corex.rpc.MethodDetail.ParamDetail;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Joshua on 2018/3/8.
 */
public class ServerRpcHandler implements RpcHandler {

    private final MethodDetail methodDetail;
    private final Object invoker;
    private final int requireType;

    public ServerRpcHandler(MethodDetail methodDetail, Object invoker, int requireType) {
        this.methodDetail = methodDetail;
        this.invoker = invoker;
        this.requireType = requireType;
    }

    public Callback<Object> handle(Auth auth, JsonObject params) throws Exception {

        // 授权类型不一致
        final int clientType = auth.getType();
        if (requireType != clientType) {

            if (clientType == ConstDefine.AUTH_TYPE_CLIENT) {
                if (requireType == ConstDefine.AUTH_TYPE_NON) {
                    // 已登录可以访问不需登录接口
                } else {
                    // 对玩家不可见
                    throw ExceptionDefine.NOT_FOUND.build();
                }
            } else if (clientType == ConstDefine.AUTH_TYPE_NON) {
                if (requireType == ConstDefine.AUTH_TYPE_CLIENT) {
                    throw ExceptionDefine.NOT_LOGIN.build();
                } else {
                    // 对玩家不可见
                    throw ExceptionDefine.NOT_FOUND.build();
                }
            } else {
                throw ExceptionDefine.NOT_AUTHORIZED.build();
            }
        }

        Object ret = invoke(params, methodDetail);

        if (ret == null) {
            if (!methodDetail.returnDetail.isVoid) {
                throw new CoreException("response is null");
            }
            return null;
        } else {
            return (Callback<Object>) ret;
        }

    }

    @Override
    public JsonObjectImpl convert(Object[] args) throws Exception {
        throw new UnsupportedOperationException("convert");
    }

    private Object invoke(JsonObject params, MethodDetail methodDetail) throws Exception {
        Object[] objects;
        if (methodDetail.params.isEmpty()) {
            objects = null;
        } else {
            objects = new Object[methodDetail.params.size()];
            int i = 0;

            for (ParamDetail pd : methodDetail.params) {
                objects[i++] = getValue(params, pd);
            }

        }

        try {
            return methodDetail.method.invoke(invoker, objects);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                e.printStackTrace();
                throw ExceptionDefine.SYSTEM_ERR.build();
            }
        }
    }

    private static List getListValue(JsonObject params, ParamDetail pd) {
        JsonArray ja = params.getJsonArray(pd.name);

        if (ja == null) {
            return Collections.EMPTY_LIST;
        }

        List list = ja.getList();
        List ret = new ArrayList(list.size());

        switch (pd.parameterizedType) {
            case BOOLEAN:
                for (Object b : list) {
                    ret.add(Json.toBoolean(b));
                }
                break;
            case INT:
                for (Object b : list) {
                    ret.add(Json.toInteger(b));
                }
                break;
            case LONG:
                for (Object b : list) {
                    ret.add(Json.toLong(b));
                }
                break;
            case DOUBLE:
                for (Object b : list) {
                    ret.add(Json.toDouble(b));
                }
                break;
            case STRING:
                for (Object b : list) {
                    ret.add(Json.toString(b));
                }
                break;
            case JOABLE:
                for (Object b : list) {
                    ret.add(Json.toJoable(b, pd.joClz));
                }
                break;
            default:
                throw new CoreException("List参数类型不合法:" + pd.parameterizedType);
        }

        return ret;
    }

    private static Object getValue(JsonObject params, ParamDetail pd) {
        if (!params.containsKey(pd.name)) {
            if (!pd.optional) {
                throw ExceptionDefine.PARAM_ERR.build();
            }
        }

        try {
            switch (pd.type) {
                case LIST:
                    return getListValue(params, pd);
                case ARRAY:
                    break;
                case BOOLEAN:
                    return params.getBoolean(pd.name, false);
                case INT:
                    return params.getInteger(pd.name, 0);
                case LONG:
                    return params.getLong(pd.name, 0L);
                case DOUBLE:
                    return params.getDouble(pd.name, 0D);
                case STRING:
                    return params.getString(pd.name, "");
                default:
                    // do nothing
                    break;
            }
        } catch (Exception e) {
            throw ExceptionDefine.PARAM_ERR.build();
        }

        throw new CoreException("参数类型不合法:" + pd.type);
    }

    @Override
    public String name() {
        return methodDetail.method.getDeclaringClass().getName() + "." + methodDetail.method.getName();
    }

    @Override
    public boolean isVoidType() {
        return methodDetail.returnDetail.isVoid;
    }
}
