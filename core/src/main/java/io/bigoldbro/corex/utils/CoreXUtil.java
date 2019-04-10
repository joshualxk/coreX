package io.bigoldbro.corex.utils;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.Handler;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.TopicDefine;
import io.bigoldbro.corex.impl.handler.PayloadCodecHandler;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.model.Push;
import io.bigoldbro.corex.model.RpcRequest;
import io.bigoldbro.corex.model.RpcResponse;
import io.bigoldbro.corex.proto.Base;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.internal.StringUtil;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Joshua on 2018/2/24.
 */
public final class CoreXUtil {

    private static Base.Auth internalAuth = Base.Auth.newBuilder()
            .setType(ConstDefine.AUTH_TYPE_INTERNAL).build();

    public static void initPipeline(ChannelPipeline p) {
        p.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
        p.addLast(new LengthFieldPrepender(4));
        p.addLast(new PayloadCodecHandler());
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

    public static Base.Auth internalAuth() {
        return internalAuth;
    }

    public static Push kickMessage(int code, String msg) {
        JsonObjectImpl jo = new JsonObjectImpl();
        jo.put("code", code);
        jo.put("msg", msg);
        return Push.newPush(TopicDefine.KICK, sysTime(), jo);
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

    public static Long sysTime() {
        return System.currentTimeMillis();
    }

}
