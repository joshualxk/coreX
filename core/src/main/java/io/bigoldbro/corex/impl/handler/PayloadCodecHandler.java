package io.bigoldbro.corex.impl.handler;

import io.bigoldbro.corex.Codec;
import io.bigoldbro.corex.model.Payload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * Created by Joshua on 2018/8/21
 */
public class PayloadCodecHandler extends ByteToMessageCodec<Payload> {

    public PayloadCodecHandler() {
    }

    private static final Codec codec = Codec.defaultCodec();

    @Override
    protected void encode(ChannelHandlerContext ctx, Payload msg, ByteBuf out) throws Exception {
        try (ByteBufOutputStream os = new ByteBufOutputStream(out)) {
            codec.writePayload(os, msg);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try (ByteBufInputStream is = new ByteBufInputStream(in)) {
            Payload payload = codec.readPayload(is);
            out.add(payload);
        }
    }
}
