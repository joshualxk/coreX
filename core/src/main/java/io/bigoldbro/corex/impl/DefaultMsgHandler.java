package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.AsyncResult;
import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.Handler;
import io.bigoldbro.corex.MsgHandler;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.proto.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultMsgHandler implements MsgHandler {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private final int maxPendingMsg;
    private final long expireTime;
    private final Map<Long, TimeHandler<AsyncResult<Base.Payload>>> handlerMap = new LinkedHashMap<>();

    public DefaultMsgHandler(int maxPendingMsg, long expireTime) {
        this.maxPendingMsg = maxPendingMsg;
        this.expireTime = expireTime;
    }

    @Override
    public void onMsgSent(long id, Handler<AsyncResult<Base.Payload>> handler) {
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
    public void onMsgReply(long id, AsyncResult<Base.Payload> resp) {
        Handler<AsyncResult<Base.Payload>> handler;
        synchronized (this) {
            handler = handlerMap.remove(id);
        }

        if (handler != null) {
            try {
                handler.handle(resp);
            } catch (Throwable t) {
                logger.debug("onMsgReply()", t);
            }
        }
    }

    public synchronized int pendingMsgNum() {
        return handlerMap.size();
    }

    @Override
    public void removeExpireMsg() {
        long now = System.currentTimeMillis();
        synchronized (this) {
            Iterator<Map.Entry<Long, TimeHandler<AsyncResult<Base.Payload>>>> it = handlerMap.entrySet().iterator();
            for (; it.hasNext(); ) {
                Map.Entry<Long, TimeHandler<AsyncResult<Base.Payload>>> entry = it.next();

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
