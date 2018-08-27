package corex.core.rpc;

import corex.core.JoHolder;
import corex.core.define.ConstDefine;
import corex.core.define.ExceptionDefine;
import corex.core.exception.CoreException;
import corex.core.json.JsonArray;
import corex.core.json.JsonObject;
import corex.core.model.Auth;
import corex.core.rpc.MethodParamDetail.ParamDetail;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 * Created by Joshua on 2018/3/8.
 */
public class ServerRpcHandler implements RpcHandler {

    private final MethodParamDetail methodParamDetail;
    private final Object invoker;
    private final int requireType;

    public ServerRpcHandler(MethodParamDetail methodParamDetail, Object invoker, int requireType) {
        this.methodParamDetail = methodParamDetail;
        this.invoker = invoker;
        this.requireType = requireType;
    }

    public JoHolder handle(Auth auth, JsonObject params) throws Exception {

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

        Object ret = invoke(params, methodParamDetail);

        if (ret == null) {
            if (!methodParamDetail.isVoidType) {
                throw new CoreException("response is null");
            }
            return null;
        } else {
            return (JoHolder) ret;
        }

    }

    @Override
    public JsonObject convert(Object[] args) throws Exception {
        throw new UnsupportedOperationException("convert");
    }

    private Object invoke(JsonObject params, MethodParamDetail methodParamDetail) throws Exception {
        Object[] objects;
        if (methodParamDetail.params.length == 0) {
            objects = null;
        } else {
            objects = new Object[methodParamDetail.params.length];
            int i = 0;

            for (ParamDetail pd : methodParamDetail.params) {
                objects[i++] = getValue(params, pd);
            }

        }

        try {
            return methodParamDetail.method.invoke(invoker, objects);
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
        JsonArray ja = params.getJsonArray(pd.param.value());

        if (ja == null) {
            return Collections.EMPTY_LIST;
        }

        switch (pd.parameterizedType) {
            case BOOLEAN:
            case INT:
            case LONG:
            case DOUBLE:
            case STRING:
                return ja.getList();
            case JO:
            default:
                break;
        }
        throw new CoreException("List参数类型不合法:" + pd.parameterizedType);
    }

    private static JsonObject getJo(JsonObject params, ParamDetail pd) {
        JsonObject jo = params.getJsonObject(pd.param.value());
        if (jo == null) {
            jo = new JsonObject();
        }

        return jo;
    }

    private static Object getValue(JsonObject params, ParamDetail pd) {
        if (!params.containsKey(pd.param.value())) {
            if (!pd.param.optional()) {
                throw ExceptionDefine.PARAM_ERR.build();
            }
        }

        try {
            switch (pd.type) {
                case LIST:
                    return getListValue(params, pd);
                case JO:
                    return getJo(params, pd);
                case ARRAY:
                    break;
                case BOOLEAN:
                    return params.getBoolean(pd.param.value(), false);
                case INT:
                    return params.getInteger(pd.param.value(), 0);
                case LONG:
                    return params.getLong(pd.param.value(), 0L);
                case DOUBLE:
                    return params.getDouble(pd.param.value(), 0D);
                case STRING:
                    return params.getString(pd.param.value(), "");
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
        return methodParamDetail.method.getDeclaringClass().getName() + "." + methodParamDetail.method.getName();
    }

    @Override
    public boolean isVoidType() {
        return methodParamDetail.isVoidType;
    }
}
