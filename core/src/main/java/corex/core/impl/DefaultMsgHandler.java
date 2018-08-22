package corex.core.impl;

import corex.core.AsyncResult;
import corex.core.Future;
import corex.core.Handler;
import corex.core.MsgHandler;
import corex.core.define.ExceptionDefine;
import corex.core.exception.CoreException;
import corex.core.model.Payload;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultMsgHandler implements MsgHandler {

    private final int maxPendingMsg;
    private final long expireTime;
    private final Map<Long, TimeHandler<AsyncResult<Payload>>> handlerMap = new LinkedHashMap<>();

    public DefaultMsgHandler(int maxPendingMsg, long expireTime) {
        this.maxPendingMsg = maxPendingMsg;
        this.expireTime = expireTime;
    }

    @Override
    public void onMsgSent(long id, Handler<AsyncResult<Payload>> handler) {
        synchronized (this) {
            if (handlerMap.size() >= maxPendingMsg) {
                throw ExceptionDefine.SYSTEM_BUSY.build();
            }
            if (handlerMap.putIfAbsent(id, new TimeHandler<>(handler, System.currentTimeMillis() + expireTime)) != null) {
                throw new CoreException("重复的id");
            }
        }
    }

    @Override
    public void onMsgReply(long id, AsyncResult<Payload> resp) {
        Handler<AsyncResult<Payload>> handler;
        synchronized (this) {
            handler = handlerMap.remove(id);
        }

        if (handler != null) {
            handler.handle(resp);
        }
    }

    public synchronized int pendingMsgNum() {
        return handlerMap.size();
    }

    @Override
    public void removeExpireMsg() {
        long now = System.currentTimeMillis();
        synchronized (this) {
            Iterator<Map.Entry<Long, TimeHandler<AsyncResult<Payload>>>> it = handlerMap.entrySet().iterator();
            for (; it.hasNext(); ) {
                Map.Entry<Long, TimeHandler<AsyncResult<Payload>>> entry = it.next();

                if (entry.getValue().expireTime > now) {
                    break;
                }
                it.remove();

                entry.getValue().handle(Future.failedFuture(ExceptionDefine.TIME_OUT.build()));
            }
        }
    }

    private static class TimeHandler<T> implements Handler<T> {

        final Handler<T> delegate;
        final long expireTime;

        public TimeHandler(Handler<T> delegate, long expireTime) {
            this.delegate = delegate;
            this.expireTime = expireTime;
        }

        @Override
        public void handle(T event) {
            delegate.handle(event);
        }
    }
}
