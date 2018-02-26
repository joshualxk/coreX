package corex.core.utils;

import corex.core.Future;
import corex.core.FutureMo;
import corex.core.Handler;
import corex.core.define.ConstDefine;
import corex.core.define.TopicDefine;
import corex.core.exception.BizEx;
import corex.proto.ModelProto.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by Joshua on 2018/2/24.
 */
public final class CoreXUtil {

    public static final Auth INTERNAL_AUTH = Auth.newBuilder().setType(ConstDefine.AUTH_TYPE_INTERNAL).build();

    public static void sendHttpError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("" + status.code() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        setHeaders(response);

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    public static void sendHttpOK(ChannelHandlerContext ctx, boolean keepAlive) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        setHeaders(response);

        ChannelFuture f = ctx.writeAndFlush(response);
        if (!keepAlive) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void setHeaders(HttpResponse httpResponse) {
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
        httpResponse.headers().set("Access-Control-Allow-Origin", "*");
        httpResponse.headers().set("Access-Control-Allow-Headers", "X-Requested-With,accept,client-os,content-type,d-version,rid,tid");
    }

    /**
     * @param localVersion  本地消息类型版本号
     * @param remoteVersion 远程消息类型版本号
     * @return 是否兼容
     */
    public static boolean isVersionCompatible(String localVersion, String remoteVersion) {
        if (StringUtil.isNullOrEmpty(localVersion) || StringUtil.isNullOrEmpty(remoteVersion)) {
            return false;
        }

        String[] splitLocalVersion = localVersion.split(".");

        String[] splitRemoteVersion = localVersion.split(".");


        if (splitLocalVersion.length < 2 || splitRemoteVersion.length < 2) {
            return false;
        }

        try {
            int localVersionNo = Integer.valueOf(splitLocalVersion[0]);
            int remoteVersionNo = Integer.valueOf(splitRemoteVersion[0]);

            return localVersionNo == remoteVersionNo;
        } catch (Exception ignore) {
        }

        return false;
    }

    public static <T> void sync(Handler<Future<T>> handler) throws Exception {
        CompletableFuture<T> future = new CompletableFuture<>();
        Future<T> fut = Future.future();
        fut.setHandler(ar -> {
            if (ar.succeeded()) {
                future.complete(ar.result());
            } else {
                future.completeExceptionally(ar.cause());
            }
        });
        handler.handle(fut);

        future.get();
    }

    public static Auth internalAuth() {
        return Auth.newBuilder().setType(ConstDefine.AUTH_TYPE_INTERNAL).build();
    }

    public static Auth adminAuth() {
        return Auth.newBuilder().setType(ConstDefine.AUTH_TYPE_ADMIN).build();
    }

    public static RpcRequest authorize(RpcRequest request, String id) {
        return request.toBuilder().setAuth(Auth.newBuilder().setType(ConstDefine.AUTH_TYPE_CLIENT).setToken(id)).build();
    }

    public static RpcRequest internalRpcRequest(int id, String module, String api, String version, BodyHolder bodyHolder) {
        return RpcRequest.newBuilder()
                .setId(id)
                .setMethod(Method.newBuilder().setModule(module).setApi(api).setVersion(version))
                .setAuth(internalAuth())
                .setTimestamp(System.currentTimeMillis())
                .setBody(bodyHolder)
                .build();
    }

    public static RpcRequest adminRpcRequest(int id, String module, String api, String version, BodyHolder bodyHolder) {
        return RpcRequest.newBuilder()
                .setId(id)
                .setMethod(Method.newBuilder().setModule(module).setApi(api).setVersion(version))
                .setAuth(adminAuth())
                .setTimestamp(System.currentTimeMillis())
                .setBody(bodyHolder)
                .build();
    }

    public static Push newPush(String topic, BodyHolder bodyHolder) {
        return Push.newBuilder()
                .setTopic(topic)
                .setTimestamp(System.currentTimeMillis())
                .setBody(bodyHolder)
                .build();
    }

    public static Broadcast internalBroadcast(int role, String topic, BodyHolder bodyHolder) {
        return Broadcast.newBuilder()
                .setInternal(true)
                .setRole(role)
                .setPush(newPush(topic, bodyHolder))
                .build();
    }

    public static Broadcast externalBroadcast(String channel, List<String> userIds, String topic, BodyHolder bodyHolder) {
        return externalBroadcast(Collections.singletonList(channel), userIds, topic, bodyHolder);
    }

    public static Broadcast externalBroadcast(List<String> channels, List<String> userIds, String topic, BodyHolder bodyHolder) {
        Broadcast.Builder builder = Broadcast.newBuilder().setRole(ConstDefine.ROLE_GATEWAY).setInternal(false);

        boolean useless = true;
        if (channels != null && !channels.isEmpty()) {
            useless = false;
            builder.addAllChannels(channels);
        }
        if (userIds != null && !userIds.isEmpty()) {
            useless = false;
            builder.addAllUserIds(userIds);
        }

        if (useless) {
            return null;
        }

        return builder.setPush(newPush(topic, bodyHolder)).build();
    }

    public static RpcResponse newRpcResponse(int id, BodyHolder bodyHolder) {
        return RpcResponse.newBuilder()
                .setId(id)
                .setTimestamp(System.currentTimeMillis())
                .setBody(bodyHolder)
                .build();
    }

    public static RpcResponse newRpcResponse(int id) {
        return RpcResponse.newBuilder()
                .setId(id)
                .setTimestamp(System.currentTimeMillis())
                .build();
    }

    public static Push kickMessage(int code, String msg) {
        FutureMo futureMapObject = FutureMo.futureMo();
        futureMapObject.putInt("code", code);
        futureMapObject.putString("msg", msg);
        return newPush(TopicDefine.KICK, futureMapObject.toBodyHolder());
    }

    public static RpcResponse bizExRpcResponse(int id, BizEx bizEx) {
        return RpcResponse.newBuilder()
                .setId(id)
                .setCode(bizEx.getCode())
                .setMsg(bizEx.getMsg())
                .setTimestamp(System.currentTimeMillis())
                .build();
    }

    public static Payload.Builder assemblePayload(long id, RpcRequest msg) {
        return Payload.newBuilder().setId(id).setRpcRequest(msg);
    }

    public static Payload.Builder assemblePayload(long id, RpcResponse msg) {
        return Payload.newBuilder().setId(id).setRpcResponse(msg);
    }

    public static Payload.Builder assemblePayload(long id, Broadcast msg) {
        return Payload.newBuilder().setId(id).setBroadcast(msg);
    }

    public static boolean needReply(long id) {
        return id != 0;
    }

    public static boolean isSuccessResponse(RpcResponse rpcResponse) {
        return rpcResponse.getCode() == 0;
    }

    public static boolean validateRpcRequest(RpcRequest request) {
        return request.getAuth().getType() == ConstDefine.AUTH_TYPE_NON;
    }

    public static boolean isRole(int target, int role) {
        return (target & role) != 0;
    }

}
