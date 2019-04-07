package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.AsyncResult;
import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.Handler;
import io.bigoldbro.corex.exception.BizException;
import io.bigoldbro.corex.exception.NoStackTraceThrowable;

/**
 * Created by Joshua on 2018/2/26.
 */
public class FutureImpl<T> implements Future<T> {

    private boolean failed;
    private boolean succeeded;
    private Handler<AsyncResult<T>> handler;
    private T result;
    private Throwable throwable;

    public FutureImpl() {
    }

    @Override
    public T result() {
        return result;
    }

    @Override
    public Throwable cause() {
        return throwable;
    }

    @Override
    public boolean succeeded() {
        return succeeded;
    }

    @Override
    public boolean failed() {
        return failed;
    }

    @Override
    public void complete(T result) {
        if (!tryComplete(result)) {
            throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
        }
    }

    @Override
    public void complete() {
        if (!tryComplete()) {
            throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
        }
    }

    @Override
    public void fail(Throwable cause) {
        if (!tryFail(cause)) {
            throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
        }
    }

    @Override
    public void fail(String failureMessage) {
        if (!tryFail(failureMessage)) {
            throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
        }
    }

    @Override
    public boolean tryComplete(T result) {
        Handler<AsyncResult<T>> h;
        synchronized (this) {
            if (succeeded || failed) {
                return false;
            }
            this.result = result;
            succeeded = true;
            h = handler;
            notifyAll();
        }
        if (h != null) {
            h.handle(this);
        }
        return true;
    }

    @Override
    public boolean tryComplete() {
        return tryComplete(null);
    }

    @Override
    public boolean tryFail(Throwable cause) {
        Handler<AsyncResult<T>> h;
        synchronized (this) {
            if (succeeded || failed) {
                return false;
            }
            this.throwable = cause != null ? cause : new NoStackTraceThrowable(null);
            failed = true;
            h = handler;
            notifyAll();
        }
        if (h != null) {
            h.handle(this);
        }
        return true;
    }

    @Override
    public boolean tryFail(String failureMessage) {
        return tryFail(new NoStackTraceThrowable(failureMessage));
    }

    @Override
    public void handle(AsyncResult<T> ar) {
        if (ar.succeeded()) {
            complete(ar.result());
        } else {
            fail(ar.cause());
        }
    }

    @Override
    public synchronized boolean isComplete() {
        return failed || succeeded;
    }

    @Override
    public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
        boolean callHandler;
        synchronized (this) {
            this.handler = handler;
            callHandler = isComplete();
        }
        if (callHandler) {
            handler.handle(this);
        }
        return this;
    }

    @Override
    public T sync() {
        Throwable cause = null;
        boolean succeeded = true;
        synchronized (this) {
            if (this.failed) {
                succeeded = false;
                cause = throwable;
            } else if (this.succeeded) {
            } else {
                try {
                    wait();
                    if (this.failed) {
                        succeeded = false;
                        cause = throwable;
                    }
                } catch (InterruptedException e) {
                    succeeded = false;
                    cause = e;
                }
            }
        }

        if (succeeded) {
            return result;
        }
        if (cause instanceof BizException) {
            throw (BizException) cause;
        } else {
            throw new RuntimeException(cause);
        }
    }

    @Override
    public String toString() {
        synchronized (this) {
            if (succeeded) {
                return "Future{result=" + result + "}";
            }
            if (failed) {
                return "Future{cause=" + throwable.getMessage() + "}";
            }
            return "Future{unresolved}";
        }
    }
}
