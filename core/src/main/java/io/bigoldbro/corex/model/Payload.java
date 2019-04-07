package io.bigoldbro.corex.model;

import io.bigoldbro.corex.json.*;

/**
 * Created by Joshua on 2018/8/21
 */
public class Payload implements Joable {
    private long id;
    private JsonArray routes;
    private int t;
    private JsonObject body;

    public Payload() {
    }

    private Payload(long id, JsonArray routes, int t, Joable body) {
        this.id = id;
        this.routes = routes;
        this.t = t;
        this.body = Json.toJsonObject(body);
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
        return Json.fromJsonObject(body, ServerAuth.class);
    }

    public Broadcast getBroadcast() {
        return Json.fromJsonObject(body, Broadcast.class);
    }

    public RpcRequest getRpcRequest() {
        return Json.fromJsonObject(body, RpcRequest.class);
    }

    public RpcResponse getRpcResponse() {
        return Json.fromJsonObject(body, RpcResponse.class);
    }

    public long getId() {
        return id;
    }

    public void readFrom(JsonObject jo) {
        id = jo.getLong("id");
        routes = jo.getJsonArray("rs");
        t = jo.getInteger("t");
        body = jo.getJsonObject("b");
    }

    @Override
    public void writeTo(JsonObject jo) {
        jo.put("id", id)
                .put("rs", routes)
                .put("t", t)
                .put("b", body);
    }
}
