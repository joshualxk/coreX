package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.json.*;
import io.bigoldbro.corex.model.Auth;
import io.bigoldbro.corex.rpc.MethodDetail.ParamDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joshua on 2018/3/8.
 */
class ClientRpcHandler implements RpcHandler {

    private final MethodDetail methodDetail;

    public ClientRpcHandler(MethodDetail methodDetail) {
        this.methodDetail = methodDetail;
    }

    @Override
    public JsonObjectImpl convert(Object[] args) throws Exception {
        if ((args == null ? 0 : args.length) != methodDetail.params.size()) {
            throw new CoreException("参数数量不一致");
        }

        JsonObjectImpl jo = new JsonObjectImpl();
        int i = 0;
        for (ParamDetail paramDetail : methodDetail.params) {
            Object arg = args[i];
            if (arg == null) {
                throw new CoreException("参数不能为空, index:" + i);
            }
            switch (paramDetail.type) {
                case LIST:
                    parseList(paramDetail, jo, arg);
                    break;
                case ARRAY:
                    parseArray(paramDetail, jo, arg);
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
            case INT:
            case LONG:
            case DOUBLE:
            case STRING:
                ja = new JsonArrayImpl((List) obj);
                break;
            case JOABLE:
                List<Joable> joableList = (List<Joable>) obj;
                List list = new ArrayList(joableList.size());
                for (Joable joable : joableList) {
                    if (joable != null) {
                        list.add(Json.toJsonObject(joable));
                    }
                }
                ja = new JsonArrayImpl(list);
                break;
            default:
                throw new CoreException("未知List类型");
        }
        jo.put(paramDetail.name, ja);

    }

    @SuppressWarnings("unchecked")
    private static void parseArray(ParamDetail paramDetail, JsonObject jo, Object obj) {
        JsonArray ja;
        switch (paramDetail.parameterizedType) {
            case BOOLEAN:
            case INT:
            case LONG:
            case DOUBLE:
            case STRING:
                ja = new JsonArrayImpl((List) obj);
                break;
            case JOABLE:
                List<Joable> joableList = (List<Joable>) obj;
                List list = new ArrayList(joableList.size());
                for (Joable joable : joableList) {
                    if (joable != null) {
                        list.add(Json.toJsonObject(joable));
                    }
                }
                ja = new JsonArrayImpl(list);
                break;
            default:
                throw new CoreException("未知List类型");
        }
        jo.put(paramDetail.name, ja);
    }

    private static void parseValue(ParamDetail paramDetail, JsonObjectImpl jo, Object obj) {
        switch (paramDetail.type) {
            case BOOLEAN:
                jo.put(paramDetail.name, (Boolean) obj);
                return;
            case INT:
                jo.put(paramDetail.name, (Integer) obj);
                return;
            case LONG:
                jo.put(paramDetail.name, (Long) obj);
                return;
            case DOUBLE:
                jo.put(paramDetail.name, (Double) obj);
                return;
            case STRING:
                jo.put(paramDetail.name, (String) obj);
                return;
        }

        throw new CoreException("未知类型");
    }

    @Override
    public String name() {
        return methodDetail.name();
    }

    @Override
    public boolean isVoidType() {
        return methodDetail.returnDetail.isVoid;
    }

    @Override
    public Callback<Object> handle(Auth auth, JsonObject params) throws Exception {
        throw new UnsupportedOperationException();
    }

}
