package corex.gateway.handler;

import corex.core.Codec;
import corex.core.ConnLifeCycle;
import corex.core.exception.CoreException;
import corex.core.utils.CoreXUtil;
import corex.proto.ModelProto.ClientPayload;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by Joshua on 2018/2/27.
 */
public class GatewayCodecHandler extends ChannelDuplexHandler {

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;

    private final Codec codec;

    private WebSocketServerHandshaker handshaker;

    private final boolean ssl;

    private final String webroot;

    public GatewayCodecHandler(Codec codec, boolean ssl, String webroot) {
        this.codec = codec;
        this.ssl = ssl;
        this.webroot = webroot;
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

    protected void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        if (!req.decoderResult().isSuccess()) {
            CoreXUtil.sendHttpError(ctx, BAD_REQUEST);
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

        final String[] splitPaths = path.split("/");
        if (splitPaths.length < 4) {
            CoreXUtil.sendHttpError(ctx, NOT_FOUND);
            return;
        }

        // /v1/h5game/ws
        if ("ws".equals(splitPaths[3])) {
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

        // 处理静态文件
        if (splitPaths[2].startsWith("static")) {
            // 去掉/v1/static前缀
            handleStaticFile(ctx, req, path.substring(2 + splitPaths[1].length() + splitPaths[2].length()));
            return;
        } else if (splitPaths[splitPaths.length - 1].startsWith("index")
                || splitPaths[splitPaths.length - 1].startsWith("entrance")
                || splitPaths[splitPaths.length - 1].startsWith("page")) {
            // 去掉/v1前缀
            handleStaticFile(ctx, req, path.substring(1 + splitPaths[1].length()));
            return;
        }

        // 非get,post请求直接返回
        if (req.method() != HttpMethod.GET && req.method() != HttpMethod.POST) {
            CoreXUtil.sendHttpOK(ctx, HttpUtil.isKeepAlive(req));
            return;
        }

        CoreXUtil.sendHttpError(ctx, HttpResponseStatus.NOT_FOUND);
    }

    protected void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof TextWebSocketFrame || frame instanceof BinaryWebSocketFrame) {
            InputStream is = null;
            try {
                is = new ByteBufInputStream(frame.content());
                ClientPayload clientPayload = codec.readClientPayload(is);
                ctx.fireChannelRead(clientPayload);
            } catch (Exception e) {
                ctx.writeAndFlush(new CloseWebSocketFrame());
            } finally {
                if (is != null) {
                    is.close();
                }
            }

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            CoreXUtil.sendHttpError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private void handleStaticFile(ChannelHandlerContext ctx, FullHttpRequest request, String path) throws IOException, ParseException {
        final String filePath = sanitizeUri(path);

        if (filePath == null) {
            CoreXUtil.sendHttpError(ctx, FORBIDDEN);
            return;
        }

        File file = new File(filePath);
        if (file.isHidden() || !file.exists() || file.isDirectory() || !file.isFile()) {
            CoreXUtil.sendHttpError(ctx, NOT_FOUND);
            return;
        }

        // Cache Validation
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(ctx);
                return;
            }
        }

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            CoreXUtil.sendHttpError(ctx, NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpUtil.setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        setDateAndCacheHeaders(response, file);
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Write the initial line and the header.
        ctx.write(response);

        // Write the content.
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        if (!ssl) {
            sendFileFuture =
                    ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
            // Write the end marker.
            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } else {
            sendFileFuture =
                    ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
                            ctx.newProgressivePromise());
            // HttpChunkedInput will write the end marker (LastHttpContent) for us.
            lastContentFuture = sendFileFuture;
        }

        // Decide whether to close the connection or not.
        if (!HttpUtil.isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    private String sanitizeUri(String uri) {
        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + '.') ||
                uri.contains('.' + File.separator) ||
                uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' ||
                INSECURE_URI.matcher(uri).matches()) {
            return null;
        }

        // Convert to absolute path.
        return webroot.replace('/', File.separatorChar) + uri;
    }

    private static void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED);
        setDateHeader(response);

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));
    }

    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(
                HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    private static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
    }

    private String getWebSocketLocation(FullHttpRequest req, String path) {
        String location = req.headers().get(HttpHeaderNames.HOST) + path;
        return (ssl ? "wss://" : "ws://") + location;
    }

}
