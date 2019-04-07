package io.bigoldbro.corex.impl.handler;

import io.bigoldbro.corex.Handler;
import io.bigoldbro.corex.model.Payload;
import io.bigoldbro.corex.model.ServerAuth;
import io.bigoldbro.corex.utils.CoreXUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Created by Joshua on 2018/3/20.
 */
public class InitialHandler extends SimpleChannelInboundHandler<Payload> {

    private static Logger LOG = LoggerFactory.getLogger(InitialHandler.class);

    private int serverId;
    private int role;
    private long startTime;
    private Handler<ServerAuth> serverAuthHandler;
    private Handler<Payload> payloadHandler;

    private boolean hasReadFirstMsg;

    public InitialHandler(int serverId, int role, long startTime) {
        this.serverId = serverId;
        this.role = role;
        this.startTime = startTime;
    }

    public void setServerAuthHandler(Handler<ServerAuth> serverAuthHandler) {
        this.serverAuthHandler = Objects.requireNonNull(serverAuthHandler);
    }

    public void setPayloadHandler(Handler<Payload> payloadHandler) {
        this.payloadHandler = Objects.requireNonNull(payloadHandler);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Payload msg) throws Exception {
        if (!hasReadFirstMsg) {
            hasReadFirstMsg = true;

            try {
                if (msg.hasServerAuth()) {
                    Handler<ServerAuth> handler = serverAuthHandler;
                    if (handler != null) {
                        // 收到auth后才算连接成功
                        handler.handle(msg.getServerAuth());
                        return;
                    }
                }
            } catch (Exception e) {
                // do nothing
            }

            ctx.close();
        } else {
            payloadHandler.handle(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.writeAndFlush(buildFirstPayload());
    }

    private Payload buildFirstPayload() {
        ServerAuth sa = new ServerAuth(serverId, role, startTime, CoreXUtil.sysTime());
        return Payload.newPayload(sa);
    }
}
