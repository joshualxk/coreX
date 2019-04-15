package io.bigoldbro.corex;

import io.bigoldbro.corex.impl.CoreXConfig;
import io.bigoldbro.corex.module.BroadcastModule;
import io.bigoldbro.corex.proto.Base;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.ExecutorService;

/**
 * Created by Joshua on 2018/2/26.
 */
public interface CoreX extends MsgHandler {

    int serverId();

    int role();

    long startTime();

    CoreXConfig config();

    EventLoopGroup acceptorEventLoopGroup();

    EventLoopGroup eventLoopGroup();

    ExecutorService executorService();

    void startService(Class<? extends Service> serviceClz, Handler<AsyncResult<String>> resultHandler) throws Exception;

    void stopService(String name, Handler<AsyncResult<Void>> resultHandler);

    void sendMessage(String address, Object msg, Handler<AsyncResult<Base.Payload>> replyHandler);

    void broadcastMessage(Base.Broadcast broadcast);

    void createNetServer(int port, ChannelHandler channelHandler, Handler<AsyncResult<Void>> resultHandler);

    void connectNetServer(String host, int port, ChannelHandler channelHandler, Handler<AsyncResult<Channel>> resultHandler);

    long setTimer(long delay, Handler<Long> handler);

    long setPeriodic(long delay, Handler<Long> handler);

    boolean cancelTimer(long id);

    <T> T asyncAgent(Class<T> clz);

    BroadcastModule broadcast();

    int subscribeBroadcast(Handler<Base.Broadcast> internal, Handler<Base.Broadcast> external);

    void onBroadcast(Base.Broadcast broadcast);

}
