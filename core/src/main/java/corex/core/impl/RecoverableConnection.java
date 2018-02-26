package corex.core.impl;

import corex.core.*;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntToLongFunction;

/**
 * Created by Joshua on 2018/3/20.
 * 可自动重连连接
 */
class RecoverableConnection extends AbstractConnection {

    private static AtomicInteger idCounter = new AtomicInteger();

    private final Context context;
    private final int serverId;
    private final int role;
    private final IntToLongFunction delayTimeFunc;
    private final Handler<RecoverableConnection> channelCloseHandler;

    private long startTime;
    private Channel channel;
    private GenericFutureListener<Future<? super Void>> closeListener;
    private int errorTimes;
    private long tid;

    public RecoverableConnection(Context context, int serverId, int role, IntToLongFunction delayTimeFunc, Handler<RecoverableConnection> channelCloseHandler) {
        super(String.valueOf(idCounter.incrementAndGet()));
        this.context = context;
        this.serverId = serverId;
        this.role = role;
        this.delayTimeFunc = delayTimeFunc;
        this.channelCloseHandler = channelCloseHandler;
    }

    public int serverId() {
        return serverId;
    }

    public int role() {
        return role;
    }

    @Override
    public void write(Object msg) {
        if (!isOpen()) {
            throw new IllegalStateException("连接未创建");
        }
        channel.writeAndFlush(msg);
    }

    public void updateChannel(Channel channel) {
        if (isOpen()) {
            throw new IllegalStateException("连接已创建");
        }

        removeChannel();
        GenericFutureListener<Future<? super Void>> closeListener = future -> context.executeFromIO(v -> triggerErrorEvent());
        channel.closeFuture().addListener(closeListener);
        this.closeListener = closeListener;
        this.channel = channel;
    }

    private void removeChannel() {
        Channel oldChannel = channel;
        GenericFutureListener<Future<? super Void>> oldCloseListener = closeListener;
        if (oldChannel != null && oldCloseListener != null) {
            oldChannel.closeFuture().removeListener(oldCloseListener);
        }
        this.channel = null;
        this.closeListener = null;
    }

    public void triggerErrorEvent() {
        onError();
        removeChannel();
        addEvent(delayTimeFunc.applyAsLong(errorTimes()), tid -> channelCloseHandler.handle(this));
    }

    private void addEvent(long delay, Handler<Long> event) {
        cancelEvent();
        tid = context.coreX().setTimer(delay, event);
    }

    private void cancelEvent() {
        long tid = this.tid;
        this.tid = 0;
        if (tid != 0) {
            context.coreX().cancelTimer(tid);
        }
    }

    public void close() {
        if (isClose()) {
            return;
        }
        changeState(ConnectionState.CLOSE);
        Channel channel = this.channel;
        removeChannel();
        if (channel != null) {
            channel.close();
        }
        cancelEvent();

        // onClose只在主动关闭时触发
        onClose();
    }

    @Override
    public boolean setSession(Session session) {
        return false;
    }

    @Override
    public Session session() {
        return null;
    }

    @Override
    public void onOpen() {
        super.onOpen();
        errorTimes = 0;
    }

    @Override
    public void onError() {
        super.onError();
        errorTimes++;
    }

    @Override
    public void onRecover() {
        super.onRecover();
        errorTimes = 0;
        cancelEvent();
    }

    public int errorTimes() {
        return errorTimes;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
