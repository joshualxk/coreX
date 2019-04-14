package io.bigoldbro.corex.rpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.json.*;
import io.bigoldbro.corex.proto.Base;
import io.bigoldbro.corex.rpc.MethodDetail.ParamDetail;

import java.nio.ByteBuffer;
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
    public Base.Body convert(Object[] args) throws Exception {
        if ((args == null ? 0 : args.length) != methodDetail.params.size()) {
            throw new CoreException("参数数量不一致");
        }

        Base.Body.Builder builder = Base.Body.newBuilder();
        int i = 0;
        for (ParamDetail paramDetail : methodDetail.params) {
            Object arg = args[i];
            if (arg == null) {
                throw new CoreException("参数不能为空, index:" + i);
            }
            switch (paramDetail.type) {
                case LIST:
                    parseList(paramDetail, arg);
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
    private static ByteString parseList(ParamDetail paramDetail, Object obj) {
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

    private static ByteString parseValue(ParamDetail paramDetail, Object obj) {
        if (paramDetail.type == ParamType.STRING) {
            return ByteString.copyFromUtf8((String) obj);
        } else if (paramDetail.type == ParamType.PROTO) {
            GeneratedMessageV3 msg = (GeneratedMessageV3) obj;
            return msg.toByteString();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        switch (paramDetail.type) {
            case BOOLEAN:
                byteBuffer.put((byte) ((Boolean) obj ? 1 : 0));
                break;
            case SHORT:
                byteBuffer.putShort((short) obj);
                break;
            case INT:
                byteBuffer.putInt((int) obj);
                break;
            case LONG:
                byteBuffer.putLong((long) obj);
                break;
            case FLOAT:
                byteBuffer.putFloat((float) obj);
                break;
            case DOUBLE:
                byteBuffer.putDouble((double) obj);
                break;
            default:
                throw new CoreException("未知类型");
        }

        byteBuffer.flip();
        return ByteString.copyFrom(byteBuffer);

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
    public Callback<Base.Body> handle(Base.Auth auth, Base.Body params) throws Exception {
        throw new UnsupportedOperationException();
    }

}
