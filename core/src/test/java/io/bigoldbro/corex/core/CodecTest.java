package io.bigoldbro.corex.core;

import com.google.protobuf.ByteString;
import io.bigoldbro.corex.proto.Base;
import io.bigoldbro.corex.utils.CoreXUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Joshua on 2018/8/24
 */
public class CodecTest {

    @Test(timeout = 5000)
    public void method1() throws InterruptedException {
        codecSpeedTest(100000);
    }

    private void codecSpeedTest(int counter) throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(counter);

        {
            Set<Integer> set = new HashSet<>(counter);
            EventLoopGroup boss = new NioEventLoopGroup(1);
            EventLoopGroup worker = new NioEventLoopGroup(1);
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            CoreXUtil.initPipeline(p);

                            p.addLast(new SimpleChannelInboundHandler<Base.Payload>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, Base.Payload msg) throws Exception {
//                                    System.out.println(msg.getId());

                                    int val = getVal(msg.getResponse().getBody());
                                    if (set.add(val)) {
                                        countDownLatch.countDown();
                                    }
                                }
                            });
                        }
                    });

            b.bind(2333).sync();
        }

        {
            EventLoopGroup worker = new NioEventLoopGroup(1);
            Bootstrap b = new Bootstrap();
            b.group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            CoreXUtil.initPipeline(p);
                        }
                    });

            Channel ch = b.connect("127.0.0.1", 2333).sync().channel();

            for (int i = 0; i < counter; ++i) {
                Base.Payload payload = Base.Payload.newBuilder()
                        .setId(i)
                        .setResponse(
                                Base.Response.newBuilder()
                                        .setId(i)
                                        .setBody(newBody(i))
                                        .setTimestamp(CoreXUtil.sysTime())
                                        .build()
                        ).build();
                ch.write(payload);

                if (i % 23 == 0) {
                    ch.flush();
                }
            }

            ch.flush();
        }

        countDownLatch.await();
    }

    private Base.Body newBody(int ix) {
        ByteBuf byteBuf = Unpooled.buffer();
        try (ByteBufOutputStream os = new ByteBufOutputStream(byteBuf)) {
            os.writeInt(ix);

            try (ByteBufInputStream is = new ByteBufInputStream(byteBuf)) {
                return Base.Body.newBuilder().addFields(ByteString.readFrom(is)).build();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getVal(Base.Body body) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(body.getFields(0).toByteArray());
        try {
            return byteBuf.readInt();
        } finally {
            byteBuf.release();
        }
    }

}
