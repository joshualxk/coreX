package corex.core.rpc;

import corex.core.FutureMo;
import corex.core.Lo;
import corex.core.Mo;
import corex.core.exception.CoreException;
import corex.core.rpc.MethodParamDetail.ParamDetail;
import corex.proto.ModelProto.Auth;

import java.util.List;

/**
 * Created by Joshua on 2018/3/8.
 */
class ClientRpcHandler implements RpcHandler {

    private final MethodParamDetail methodParamDetail;

    public ClientRpcHandler(MethodParamDetail methodParamDetail) {
        this.methodParamDetail = methodParamDetail;
    }

    @Override
    public FutureMo convert(Object[] args) throws Exception {
        if ((args == null ? 0 : args.length) != methodParamDetail.params.length) {
            throw new CoreException("参数数量不一致");
        }

        FutureMo futureMapObject = FutureMo.futureMo();
        int i = 0;
        for (ParamDetail paramDetail : methodParamDetail.params) {
            Object arg = args[i];
            if (arg == null) {
                throw new CoreException("参数不能为空, index:" + i);
            }
            switch (paramDetail.type) {
                case LIST:
                    parseList(paramDetail, futureMapObject, arg);
                    break;
                case MO:
                    parseMo(paramDetail, futureMapObject, arg);
                    break;
                case ARRAY:
                    break;
                default:
                    parseValue(paramDetail, futureMapObject, arg);
                    break;
            }
            ++i;
        }
        return futureMapObject;
    }

    @SuppressWarnings("unchecked")
    private static void parseList(ParamDetail paramDetail, FutureMo futureMapObject, Object obj) {
        Lo lo = Lo.lo();
        switch (paramDetail.parameterizedType) {
            case BOOLEAN:
                lo.setBooleanList((List<Boolean>) obj);
                break;
            case INT:
                lo.setIntList((List<Integer>) obj);
                break;
            case LONG:
                lo.setLongList((List<Long>) obj);
                break;
            case DOUBLE:
                lo.setDoubleList((List<Double>) obj);
                break;
            case STRING:
                lo.setStringList((List<String>) obj);
                break;
            case MO:
                lo.setMoList((List<Mo>) obj);
            default:
                throw new CoreException("未知List类型");
        }
        futureMapObject.putList(paramDetail.param.value(), lo);

    }

    private static void parseMo(ParamDetail paramDetail, FutureMo futureMapObject, Object obj) {
        futureMapObject.putMo(paramDetail.param.value(), (Mo) obj);
    }

    private static void parseValue(ParamDetail paramDetail, FutureMo futureMapObject, Object obj) {
        switch (paramDetail.type) {
            case BOOLEAN:
                futureMapObject.putBoolean(paramDetail.param.value(), (Boolean) obj);
                return;
            case INT:
                futureMapObject.putInt(paramDetail.param.value(), (Integer) obj);
                return;
            case LONG:
                futureMapObject.putLong(paramDetail.param.value(), (Long) obj);
                return;
            case DOUBLE:
                futureMapObject.putDouble(paramDetail.param.value(), (Double) obj);
                return;
            case STRING:
                futureMapObject.putString(paramDetail.param.value(), (String) obj);
                return;
        }

        throw new CoreException("未知类型");
    }

    @Override
    public String name() {
        return methodParamDetail.name();
    }

    @Override
    public boolean isVoidType() {
        return methodParamDetail.isVoidType;
    }

    @Override
    public FutureMo handle(Auth auth, Mo params) throws Exception {
        throw new UnsupportedOperationException();
    }
}
