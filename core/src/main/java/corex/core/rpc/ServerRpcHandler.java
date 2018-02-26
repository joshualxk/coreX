package corex.core.rpc;

import corex.core.FutureMo;
import corex.core.Lo;
import corex.core.Mo;
import corex.core.define.ConstDefine;
import corex.core.define.ExceptionDefine;
import corex.core.exception.CoreException;
import corex.core.rpc.MethodParamDetail.ParamDetail;
import corex.proto.ModelProto.Auth;

import java.lang.reflect.InvocationTargetException;

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

    public FutureMo handle(Auth auth, Mo params) throws Exception {

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
            return (FutureMo) ret;
        }

    }

    @Override
    public FutureMo convert(Object[] args) throws Exception {
        throw new UnsupportedOperationException("convert");
    }

    private Object invoke(Mo params, MethodParamDetail methodParamDetail) throws Exception {
        if (methodParamDetail.params.length == 0) {
            return methodParamDetail.method.invoke(invoker);
        } else {
            Object[] objects = new Object[methodParamDetail.params.length];
            int i = 0;

            for (ParamDetail pd : methodParamDetail.params) {
                objects[i++] = getValue(params, pd);
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
    }

    private static Object getListValue(Mo params, ParamDetail pd) {
        Lo lo = params.getList(pd.param.value());
        switch (pd.parameterizedType) {
            case BOOLEAN:
                return lo.getBooleanList();
            case INT:
                return lo.getIntList();
            case LONG:
                return lo.getLongList();
            case DOUBLE:
                return lo.getDoubleList();
            case STRING:
                return lo.getStringList();
            case MO:
                return lo.getMoList();
        }
        throw new CoreException("List参数类型不合法:" + pd.parameterizedType);
    }

    private static Object getMo(Mo params, ParamDetail pd) {
        return params.getMo(pd.param.value());
    }

    private static Object getValue(Mo params, ParamDetail pd) {
        if (!params.containsKey(pd.param.value())) {
            if (!pd.param.optional()) {
                throw ExceptionDefine.PARAM_ERR.build();
            }
        }

        try {
            switch (pd.type) {
                case LIST:
                    return getListValue(params, pd);
                case MO:
                    return getMo(params, pd);
                case ARRAY:
                    break;
                case BOOLEAN:
                    return params.getBooleanOrDefault(pd.param.value(), false);
                case INT:
                    return params.getIntOrDefault(pd.param.value(), 0);
                case LONG:
                    return params.getLongOrDefault(pd.param.value(), 0L);
                case DOUBLE:
                    return params.getDoubleOrDefault(pd.param.value(), 0D);
                case STRING:
                    return params.getStringOrDefault(pd.param.value(), "");
                default:
                    // do nothing
                    break;
            }
        } catch (IllegalArgumentException e) {
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
