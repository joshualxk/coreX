package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.AbstractConnection;
import io.bigoldbro.corex.Codec;
import io.bigoldbro.corex.Session;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.model.ClientPayload;
import io.bigoldbro.corex.model.Push;
import io.bigoldbro.corex.model.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;

/**
 * Created by Joshua on 2018/3/1.
 */
public class WebSocketConnection extends AbstractConnection {

    private static final Codec codec = Codec.defaultCodec();

    private final Channel channel;

    private static final AttributeKey<Session> SESSION = AttributeKey.valueOf("coreX.session");

    public WebSocketConnection(Channel channel) {
        super(channelId(channel));
        this.channel = channel;
    }

    @Override
    public void write(Object msg) {
        ClientPayload clientPayload;
        if (msg instanceof RpcResponse) {
            RpcResponse response = (RpcResponse) msg;
            clientPayload = ClientPayload.newClientPayload(response);
        } else if (msg instanceof Push) {
            Push push = (Push) msg;
            clientPayload = ClientPayload.newClientPayload(push);
        } else {
            throw new CoreException("未知类型:" + msg.getClass().getName());
        }

        ByteBuf byteBuf = null;
        try {
            byteBuf = channel.alloc().ioBuffer();

            try (ByteBufOutputStream os = new ByteBufOutputStream(byteBuf)) {
                codec.writeClientPayload(os, clientPayload);
            }

            WebSocketFrame webSocketFrame = new TextWebSocketFrame(byteBuf);
            channel.writeAndFlush(webSocketFrame);
            byteBuf = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
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

    @Override
    public void onMsg(Object msg) {
        if (msg instanceof ByteBuf) {
            try (ByteBufInputStream is = new ByteBufInputStream((ByteBuf) msg)) {
                ClientPayload clientPayload = codec.readClientPayload(is);
                if (!clientPayload.hasRpcRequest()) {
                    close();
                    return;
                }

                msg = clientPayload;
            } catch (Exception e) {
                close();
                return;
            }
        }
        super.onMsg(msg);
    }
}
