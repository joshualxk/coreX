package io.bigoldbro.corex.model;

import io.bigoldbro.corex.json.Joable;
import io.bigoldbro.corex.json.Json;
import io.bigoldbro.corex.json.JsonObject;

/**
 * Created by Joshua on 2018/8/21
 */
public class ClientPayload implements Joable {
    private int t;
    private JsonObject body;

    private ClientPayload(int t, Joable body) {
        this.t = t;
        this.body = Json.toJsonObject(body);
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

    public void readFrom(JsonObject jo) {
        t = jo.getInteger("t");
        body = jo.getJsonObject("b");
    }

    @Override
    public void writeTo(JsonObject jo) {
        jo.put("t", t)
                .put("b", body);
    }
}
