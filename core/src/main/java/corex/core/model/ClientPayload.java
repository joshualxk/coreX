package corex.core.model;

import corex.core.Joable;
import corex.core.exception.DecodeException;
import corex.core.json.JsonObject;

/**
 * Created by Joshua on 2018/8/21
 */
public class ClientPayload implements Joable {
    private final int t;
    private final Joable body;

    private ClientPayload(int t, Joable body) {
        this.t = t;
        this.body = body;
    }

    public static ClientPayload newClientPayload(RpcRequest rpcRequest) {
        return new ClientPayload(RpcRequest.T, rpcRequest);
    }

    public static ClientPayload newClientPayload(RpcResponse rpcResponse) {
        return new ClientPayload(RpcResponse.T, rpcResponse);
    }

    public static ClientPayload newClientPayload(Push push) {
        return new ClientPayload(Push.T, push);
    }

    public boolean hasRpcRequest() {
        return t == RpcRequest.T;
    }

    public boolean hasRpcResponse() {
        return t == RpcResponse.T;
    }

    public boolean hasPush() {
        return t == Push.T;
    }

    public RpcRequest getRpcRequest() {
        return (RpcRequest) body;
    }

    public RpcResponse getRpcResponse() {
        return (RpcResponse) body;
    }

    public Push getPush() {
        return (Push) body;
    }

    public static ClientPayload fromJo(JsonObject jo) throws Exception {
        int t = jo.getInteger("t");
        JsonObject bodyJo = jo.getJsonObject("b");

        Joable body;
        switch (t) {
            case RpcRequest.T:
                body = RpcRequest.fromJo(bodyJo);
                break;
            case RpcResponse.T:
                body = RpcResponse.fromJo(bodyJo);
                break;
            case Push.T:
                body = Push.fromJo(bodyJo);
                break;
            default:
                throw new DecodeException("未知ClientPayload类型:" + t);
        }
        return new ClientPayload(t, body);
    }

    @Override
    public JsonObject toJo() {
        return new JsonObject()
                .put("t", t)
                .put("b", body.toJo());
    }
}
