package corex.core.impl.handler;

import corex.core.ConnLifeCycle;
import corex.core.Connection;
import corex.core.Context;
import corex.core.Handler;
import corex.core.exception.CoreException;
import corex.core.impl.WebSocketConnection;
import corex.proto.ModelProto.ClientPayload;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * Created by Joshua on 2018/2/28.
 */
@ChannelHandler.Sharable
public class BridgeHandler extends SimpleChannelInboundHandler<ClientPayload> {

    private static final AttributeKey<Connection> CONNECTION_KEY = AttributeKey.valueOf("coreX.connection");

    protected final Context context;
    protected final Handler<Connection> openHandler;

    public BridgeHandler(Context context, Handler<Connection> openHandler) {
        this.context = context;
        this.openHandler = openHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientPayload msg) throws Exception {
        triggerConnectionMsg(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof ConnLifeCycle) {
            if (evt == ConnLifeCycle.WS_OPEN) {
                Connection conn = new WebSocketConnection(ctx.channel(), context.coreX().codec());
                triggerConnectionOpen(ctx, conn);
                return;
            }

            if (evt == ConnLifeCycle.WS_CLOSE) {
                triggerConnectionClose(ctx);
                return;
            }
        }
    }

    private void triggerConnectionOpen(ChannelHandlerContext ctx, Connection conn) throws Exception {
        Attribute<Connection> attributeKey = ctx.channel().attr(CONNECTION_KEY);
        if (!attributeKey.compareAndSet(null, conn)) {
            throw new Exception("连接已存在");
        }
        context.executeFromIO(v -> {
            conn.openHandler(openHandler);
            conn.onOpen();
        });
    }

    private void triggerConnectionClose(ChannelHandlerContext ctx) throws Exception {
        Attribute<Connection> attributeKey = ctx.channel().attr(CONNECTION_KEY);
        Connection conn = attributeKey.get();
        if (conn != null) {
            attributeKey.set(null);
            context.executeFromIO(v -> {
                conn.onClose();
            });

        } else {
            throw new Exception("连接不存在");
        }
    }

    private void triggerConnectionMsg(ChannelHandlerContext ctx, ClientPayload msg) {
        Connection conn = ctx.channel().attr(CONNECTION_KEY).get();
        if (conn == null) {
            throw new CoreException("连接不存在");
        } else {
            context.executeFromIO(v -> {
                conn.onMsg(msg);
            });

        }
    }
}
