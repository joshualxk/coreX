package io.bigoldbro.corex.model;

import io.bigoldbro.corex.exception.DecodeException;
import io.bigoldbro.corex.json.Joable;
import io.bigoldbro.corex.json.JsonArray;
import io.bigoldbro.corex.json.JsonArrayImpl;
import io.bigoldbro.corex.json.JsonObjectImpl;

/**
 * Created by Joshua on 2018/8/21
 */
public class Payload {
    private long id;
    private JsonArray routes;
    private int t;
    private Joable body;

    private Payload(long id, JsonArray routes, int t, Joable body) {
        this.id = id;
        this.routes = routes;
        this.t = t;
        this.body = body;
    }

    public static Payload newPayload(ServerAuth serverAuth) {
        return new Payload(0, new JsonArrayImpl(), ServerAuth.T, serverAuth);
    }

    public static Payload newPayload(long id, Broadcast broadcast) {
        return new Payload(id, new JsonArrayImpl(), Broadcast.T, broadcast);
    }

    public static Payload newPayload(long id, RpcRequest rpcRequest) {
        return new Payload(id, new JsonArrayImpl(), RpcRequest.T, rpcRequest);
    }

    public static Payload newPayload(long id, RpcResponse rpcResponse) {
        return new Payload(id, new JsonArrayImpl(), RpcResponse.T, rpcResponse);
    }

    public Payload addRoute(String route) {
        this.routes.add(route);
        return this;
    }

    public boolean hasServerAuth() {
        return t == ServerAuth.T;
    }

    public boolean hasBroadcast() {
        return t == Broadcast.T;
    }

    public boolean hasRpcRequest() {
        return t == RpcRequest.T;
    }

    public boolean hasRpcResponse() {
        return t == RpcResponse.T;
    }

    public ServerAuth getServerAuth() {
        return (ServerAuth) body;
    }

    public Broadcast getBroadcast() {
        return (Broadcast) body;
    }

    public RpcRequest getRpcRequest() {
        return (RpcRequest) body;
    }

    public RpcResponse getRpcResponse() {
        return (RpcResponse) body;
    }

    public long getId() {
        return id;
    }

    public void readFrom(JsonObjectImpl jo) throws Exception {
        id = jo.getLong("id");
        routes = jo.getJsonArray("rs");
        t = jo.getInteger("t");
        JsonObjectImpl bodyJo = jo.getJsonObject("b");

        Joable body;
        switch (t) {
            case ServerAuth.T:
                body = ServerAuth.fromJo(bodyJo);
                break;
            case Broadcast.T:
                body = Broadcast.fromJo(bodyJo);
                break;
            case RpcRequest.T:
                body = RpcRequest.fromJo(bodyJo);
                break;
            case RpcResponse.T:
                body = RpcResponse.fromJo(bodyJo);
                break;
            default:
                throw new DecodeException("未知Payload类型:" + t);
        }
        return new Payload(id, routes, t, body);
    }

    @Override
    public JsonObjectImpl toJo() {
        return new JsonObjectImpl()
                .put("id", id)
                .put("rs", routes)
                .put("t", t)
                .put("b", body.toJo());
    }
}
