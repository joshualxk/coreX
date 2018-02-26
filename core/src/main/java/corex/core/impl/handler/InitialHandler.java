package corex.core.impl.handler;

import corex.core.Handler;
import corex.core.exception.CoreException;
import corex.proto.ModelProto.Payload;
import corex.proto.ModelProto.Ping;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Objects;

/**
 * Created by Joshua on 2018/3/20.
 */
public class InitialHandler extends SimpleChannelInboundHandler<Payload> {

    private int serverId;
    private int role;
    private long startTime;
    private Handler<Ping> firstPingHandler;
    private Handler<Payload> payloadHandler;

    private boolean hasReadFirstMsg;

    public InitialHandler(int serverId, int role, long startTime) {
        this.serverId = serverId;
        this.role = role;
        this.startTime = startTime;
    }

    public void setFirstPingHandler(Handler<Ping> firstPingHandler) {
        this.firstPingHandler = Objects.requireNonNull(firstPingHandler);
    }

    public void setPayloadHandler(Handler<Payload> payloadHandler) {
        this.payloadHandler = Objects.requireNonNull(payloadHandler);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Payload msg) throws Exception {
        if (!hasReadFirstMsg) {
            hasReadFirstMsg = true;
            if (msg.hasPing()) {
                Handler<Ping> handler = firstPingHandler;
                if (handler != null) {
                    // 收到ping后才算连接成功
                    handler.handle(msg.getPing());
                } else {
                    throw new CoreException("firstPingHandler未初始化");
                }
            } else {
                ctx.close();
            }
        } else {
            payloadHandler.handle(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.writeAndFlush(newPing());
    }

    private Payload newPing() {
        return Payload.newBuilder().setPing(
                Ping.newBuilder()
                        .setServerId(serverId)
                        .setRole(role)
                        .setStartTime(startTime)
                        .setTimestamp(System.currentTimeMillis())
                        .build()
        ).build();
    }
}
