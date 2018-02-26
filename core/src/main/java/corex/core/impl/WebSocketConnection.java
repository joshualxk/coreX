package corex.core.impl;

import corex.core.AbstractConnection;
import corex.core.Codec;
import corex.core.Connection;
import corex.core.Session;
import corex.core.exception.CoreException;
import corex.proto.ModelProto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Joshua on 2018/3/1.
 */
public class WebSocketConnection extends AbstractConnection {

    private final Channel channel;
    private final Codec codec;

    static final AttributeKey<Session> SESSION = AttributeKey.valueOf("coreX.session");

    public WebSocketConnection(Channel channel, Codec codec) {
        super(channelId(channel));
        this.channel = channel;
        this.codec = codec;
    }

    @Override
    public void write(Object msg) {
        ModelProto.ClientPayload clientPayload;
        if (msg instanceof ModelProto.RpcResponse) {
            ModelProto.RpcResponse response = (ModelProto.RpcResponse) msg;
            clientPayload = ModelProto.ClientPayload.newBuilder().setRpcResponse(response).build();
        } else if (msg instanceof ModelProto.Push) {
            ModelProto.Push push = (ModelProto.Push) msg;
            clientPayload = ModelProto.ClientPayload.newBuilder().setPush(push).build();
        } else {
            throw new CoreException("未知类型:" + msg.getClass().getName());
        }

        ByteBuf byteBuf = null;
        OutputStream os = null;
        try {
            byteBuf = channel.alloc().ioBuffer();
            os = new ByteBufOutputStream(byteBuf);
            codec.writeClientPayload(os, clientPayload);

            WebSocketFrame webSocketFrame = new BinaryWebSocketFrame(byteBuf);
            channel.writeAndFlush(webSocketFrame);
            byteBuf = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    @Override
    public boolean setSession(Session session) {
        if (session == null) {
            return channel.attr(SESSION).getAndSet(null) != null;
        } else {
            return channel.attr(SESSION).setIfAbsent(session) == null;
        }
    }

    @Override
    public Session session() {
        return channel.attr(SESSION).get();
    }

    @Override
    public void close() {
        channel.writeAndFlush(new CloseWebSocketFrame());
    }

}
