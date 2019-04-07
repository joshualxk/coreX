package io.bigoldbro.corex;

import io.bigoldbro.corex.model.Broadcast;
import io.bigoldbro.corex.model.Payload;
import io.bigoldbro.corex.module.BroadcastModule;
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

    void startService(Service service, Handler<AsyncResult<Void>> resultHandler);

    void stopService(String name, Handler<AsyncResult<Void>> resultHandler);

    void sendMessage(String address, Object msg, Handler<AsyncResult<Payload>> replyHandler);

    void broadcastMessage(Broadcast broadcast);

    void createNetServer(int port, ChannelHandler channelHandler, Handler<AsyncResult<Void>> resultHandler);

    void connectNetServer(String host, int port, ChannelHandler channelHandler, Handler<AsyncResult<Channel>> resultHandler);

    long setTimer(long delay, Handler<Long> handler);

    long setPeriodic(long delay, Handler<Long> handler);

    boolean cancelTimer(long id);

    <T> T asyncAgent(Class<T> clz);

    BroadcastModule broadcast();

    int subscribeBroadcast(Handler<Broadcast> internal, Handler<Broadcast> external);

    void onBroadcast(Broadcast broadcast);

}
