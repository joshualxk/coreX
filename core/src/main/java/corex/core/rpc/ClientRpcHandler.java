package corex.core.rpc;

import corex.core.JoHolder;
import corex.core.exception.CoreException;
import corex.core.json.JsonArray;
import corex.core.json.JsonObject;
import corex.core.model.Auth;
import corex.core.rpc.MethodParamDetail.ParamDetail;

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
    public JsonObject convert(Object[] args) throws Exception {
        if ((args == null ? 0 : args.length) != methodParamDetail.params.length) {
            throw new CoreException("参数数量不一致");
        }

        JsonObject jo = new JsonObject();
        int i = 0;
        for (ParamDetail paramDetail : methodParamDetail.params) {
            Object arg = args[i];
            if (arg == null) {
                throw new CoreException("参数不能为空, index:" + i);
            }
            switch (paramDetail.type) {
                case LIST:
                    parseList(paramDetail, jo, arg);
                    break;
                case JO:
                    parseJo(paramDetail, jo, arg);
                    break;
                case ARRAY:
                    break;
                default:
                    parseValue(paramDetail, jo, arg);
                    break;
            }
            ++i;
        }
        return jo;
    }

    @SuppressWarnings("unchecked")
    private static void parseList(ParamDetail paramDetail, JsonObject jo, Object obj) {
        JsonArray ja;
        switch (paramDetail.parameterizedType) {
            case BOOLEAN:
                ja = new JsonArray((List<Boolean>) obj);
                break;
            case INT:
                ja = new JsonArray((List<Integer>) obj);
                break;
            case LONG:
                ja = new JsonArray((List<Long>) obj);
                break;
            case DOUBLE:
                ja = new JsonArray((List<Double>) obj);
                break;
            case STRING:
                ja = new JsonArray((List<String>) obj);
                break;
            case JO:
            default:
                throw new CoreException("未知List类型");
        }
        jo.put(paramDetail.param.value(), ja);

    }

    private static void parseJo(ParamDetail paramDetail, JsonObject jo, Object obj) {
        jo.put(paramDetail.param.value(), (JsonObject) obj);
    }

    private static void parseValue(ParamDetail paramDetail, JsonObject jo, Object obj) {
        switch (paramDetail.type) {
            case BOOLEAN:
                jo.put(paramDetail.param.value(), (Boolean) obj);
                return;
            case INT:
                jo.put(paramDetail.param.value(), (Integer) obj);
                return;
            case LONG:
                jo.put(paramDetail.param.value(), (Long) obj);
                return;
            case DOUBLE:
                jo.put(paramDetail.param.value(), (Double) obj);
                return;
            case STRING:
                jo.put(paramDetail.param.value(), (String) obj);
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
    public JoHolder handle(Auth auth, JsonObject params) throws Exception {
        throw new UnsupportedOperationException();
    }
}
