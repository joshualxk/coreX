package corex.core.model;

import corex.core.Joable;
import corex.core.exception.DecodeException;
import corex.core.json.JsonArray;
import corex.core.json.JsonObject;

/**
 * Created by Joshua on 2018/8/21
 */
public class Payload implements Joable {
    private final long id;
    private final JsonArray routes;
    private final int t;
    private final Joable body;

    private Payload(long id, JsonArray routes, int t, Joable body) {
        this.id = id;
        this.routes = routes;
        this.t = t;
        this.body = body;
    }

    public static Payload newPayload(ServerAuth serverAuth) {
        return new Payload(0, new JsonArray(), ServerAuth.T, serverAuth);
    }

    public static Payload newPayload(long id, Broadcast broadcast) {
        return new Payload(id, new JsonArray(), Broadcast.T, broadcast);
    }

    public static Payload newPayload(long id, RpcRequest rpcRequest) {
        return new Payload(id, new JsonArray(), RpcRequest.T, rpcRequest);
    }

    public static Payload newPayload(long id, RpcResponse rpcResponse) {
        return new Payload(id, new JsonArray(), RpcResponse.T, rpcResponse);
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

    public static Payload fromJo(JsonObject jo) throws Exception {
        long id = jo.getLong("id");
        JsonArray routes = jo.getJsonArray("rs");
        int t = jo.getInteger("t");
        JsonObject bodyJo = jo.getJsonObject("b");

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
    public JsonObject toJo() {
        return new JsonObject()
                .put("id", id)
                .put("rs", routes)
                .put("t", t)
                .put("b", body.toJo());
    }
}
