package io.bigoldbro.corex.core;

import io.bigoldbro.corex.json.JsonArrayImpl;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.model.Payload;
import io.bigoldbro.corex.model.RpcResponse;
import io.bigoldbro.corex.utils.CoreXUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Joshua on 2018/8/24
 */
public class CodecTest {

    @Test
    public void method1() throws InterruptedException {
    }

    @Test
    public void method2() throws InterruptedException {
        codecSpeedTest(20000);
    }

    private void codecSpeedTest(int counter) throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(counter);

        {
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

                            p.addLast(new SimpleChannelInboundHandler<Payload>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, Payload msg) throws Exception {
//                                    System.out.println(msg.getId());

                                    int ix = msg.getRpcResponse().getBody().getInteger("ix");
                                    countDownLatch.countDown();
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
                Payload payload = Payload.newPayload(i, RpcResponse.newSuccessRpcResponse(i, newJo(i)));
                ch.write(payload);

                if (i % 23 == 0) {
                    ch.flush();
                }
            }

            ch.flush();
        }

        countDownLatch.await();
    }

    private JsonObjectImpl newJo(int ix) {
        return new JsonObjectImpl()
                .put("ix", ix)
                .put("param1", ix)
                .put("param2", "str_" + ix)
                .put("arr", new JsonArrayImpl().add(1).add(2))
                .put("f1", 0.1f + ix);
    }

    @Test
    public void nullTest() {
        String s = "";
//        JsonObjectImpl jo1 = new JsonObjectImpl(s);
//        System.out.println(jo1);

        s = null;
        JsonObjectImpl jo2 = new JsonObjectImpl(s);
        System.out.println(jo2);

    }


}
