package corex.gateway.handler;

import corex.core.Codec;
import corex.core.ConnLifeCycle;
import corex.core.exception.CoreException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by Joshua on 2018/2/27.
 */
public class GatewayHttpHandler extends ChannelDuplexHandler {

    private final Codec codec;

    private WebSocketServerHandshaker handshaker;

    private final boolean ssl;

    public GatewayHttpHandler(Codec codec, boolean ssl) {
        this.codec = codec;
        this.ssl = ssl;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        } else {
            throw new CoreException("未知类型消息");
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        if (!req.decoderResult().isSuccess()) {
            sendHttpError(ctx, BAD_REQUEST);
            return;
        }

        final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
        final String path = queryStringDecoder.path();

        // nginx检测页
        if ("/ping".equals(path)) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer("pong", CharsetUtil.UTF_8));
            HttpUtil.setContentLength(response, response.content().readableBytes());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        // xxx/wsapp
        if (path.endsWith("/wsapp")) {
            // Handshake
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req, path), null, true, 5 * 1024 * 1024);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req).addListener(future -> {
                    if (future.isSuccess()) {
                        ctx.fireUserEventTriggered(ConnLifeCycle.WS_OPEN);
                        ctx.channel().closeFuture().addListener(fut -> {
                            ctx.fireUserEventTriggered(ConnLifeCycle.WS_CLOSE);
                        });
                    } else {
                        future.cause().printStackTrace();
                    }
                });
            }
            return;
        }

        // 非get,post请求直接返回
        if (req.method() != HttpMethod.GET && req.method() != HttpMethod.POST) {
            sendHttpOK(ctx, HttpUtil.isKeepAlive(req));
            return;
        }

        sendHttpError(ctx, HttpResponseStatus.NOT_FOUND);
    }

    protected void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame || frame instanceof BinaryWebSocketFrame) {
            return;
        }
        if (frame instanceof TextWebSocketFrame) {
            try {
                ctx.fireChannelRead(frame.content());
            } catch (Exception e) {
                ctx.writeAndFlush(new CloseWebSocketFrame());
            }

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendHttpError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private String getWebSocketLocation(FullHttpRequest req, String path) {
        String location = req.headers().get(HttpHeaderNames.HOST) + path;
        return (ssl ? "wss://" : "ws://") + location;
    }

    private static void sendHttpError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("" + status.code() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        setHeaders(response);

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendHttpOK(ChannelHandlerContext ctx, boolean keepAlive) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        setHeaders(response);

        ChannelFuture f = ctx.writeAndFlush(response);
        if (!keepAlive) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static void setHeaders(HttpResponse httpResponse) {
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
        httpResponse.headers().set("Access-Control-Allow-Origin", "*");
        httpResponse.headers().set("Access-Control-Allow-Headers", "X-Requested-With,accept,client-os,content-type,d-version,rid,tid");
    }
}
