package io.bigoldbro.corex.utils;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.Handler;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.TopicDefine;
import io.bigoldbro.corex.exception.BizEx;
import io.bigoldbro.corex.impl.handler.PayloadCodecHandler;
import io.bigoldbro.corex.model.ErrorMsg;
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
        fut.addHandler(ar -> {
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

    public static Base.Push kickMessage(int code, String msg) {
        return Base.Push.newBuilder()
                .setTopic(TopicDefine.KICK)
                .setTimestamp(sysTime())
                .setBody(new ErrorMsg(code, msg).toBody())
                .build();
    }

    public static Base.Response successResponse(int id) {
        return Base.Response.newBuilder()
                .setId(id)
                .setTimestamp(sysTime())
                .build();
    }

    public static Base.Response failedResponse(int id, BizEx bizEx) {
        return Base.Response.newBuilder()
                .setId(id)
                .setCode(bizEx.getCode())
                .setMsg(bizEx.getMessage())
                .setTimestamp(sysTime())
                .build();
    }

    public static Base.Response newResponse(int id, Base.Body body) {
        return Base.Response.newBuilder()
                .setId(id)
                .setTimestamp(sysTime())
                .setBody(body)
                .build();
    }

    public static Base.Method newMethod(String module, String api, String version) {
        return Base.Method.newBuilder()
                .setModule(module)
                .setApi(api)
                .setVersion(version)
                .build();
    }

    public static Base.Request newRequest(int id, Base.Auth auth, Base.Method method, Base.Body body) {
        return Base.Request.newBuilder()
                .setId(id)
                .setAuth(auth)
                .setMethod(method)
                .setBody(body)
                .setTimestamp(sysTime())
                .build();
    }

    public static Base.Payload newPayload(long id, Base.Response response) {
        return Base.Payload.newBuilder()
                .setId(id)
                .setResponse(response)
                .build();
    }

    public static boolean needReply(long id) {
        return id != 0;
    }

    public static boolean isSuccessResponse(Base.Response rpcResponse) {
        return rpcResponse.getCode() == 0;
    }

    public static boolean isRole(int target, int role) {
        return (target & role) != 0;
    }

    public static long sysTime() {
        return System.currentTimeMillis();
    }

}
