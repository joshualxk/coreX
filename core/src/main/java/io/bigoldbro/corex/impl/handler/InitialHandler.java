package io.bigoldbro.corex.impl.handler;

import io.bigoldbro.corex.Handler;
import io.bigoldbro.corex.proto.Base;
import io.bigoldbro.corex.utils.CoreXUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Created by Joshua on 2018/3/20.
 */
public class InitialHandler extends SimpleChannelInboundHandler<Base.Payload> {

    private static Logger LOG = LoggerFactory.getLogger(InitialHandler.class);

    private int serverId;
    private int role;
    private long startTime;
    private Handler<Base.Ping> serverAuthHandler;
    private Handler<Base.Payload> payloadHandler;

    private boolean hasReadFirstMsg;

    public InitialHandler(int serverId, int role, long startTime) {
        this.serverId = serverId;
        this.role = role;
        this.startTime = startTime;
    }

    public void setServerAuthHandler(Handler<Base.Ping> serverAuthHandler) {
        this.serverAuthHandler = Objects.requireNonNull(serverAuthHandler);
    }

    public void setPayloadHandler(Handler<Base.Payload> payloadHandler) {
        this.payloadHandler = Objects.requireNonNull(payloadHandler);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Base.Payload msg) throws Exception {
        if (!hasReadFirstMsg) {
            hasReadFirstMsg = true;

            try {
                if (msg.hasPing()) {
                    Handler<Base.Ping> handler = serverAuthHandler;
                    if (handler != null) {
                        // 收到auth后才算连接成功
                        handler.handle(msg.getPing());
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

    private Base.Payload buildFirstPayload() {
        Base.Ping ping = Base.Ping.newBuilder()
                .setServerId(serverId)
                .setRole(role)
                .setStartTime(startTime)
                .setTimestamp(CoreXUtil.sysTime())
                .build();

        return Base.Payload.newBuilder()
                .setPing(ping).build();
    }
}
