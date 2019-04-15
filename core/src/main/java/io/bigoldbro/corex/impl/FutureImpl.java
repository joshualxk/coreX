package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.AsyncResult;
import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.Handler;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.exception.NoStackTraceThrowable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by Joshua on 2018/2/26.
 */
public class FutureImpl<T> extends AbstractFuture<T> {

    private boolean failed;
    private boolean succeeded;
    private List<Handler<AsyncResult<T>>> handlers;
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
        synchronized (this) {
            if (succeeded || failed) {
                return false;
            }
            this.result = result;
            succeeded = true;
            notifyAll();
        }
        doAllHandlers();
        return true;
    }

    @Override
    public boolean tryComplete() {
        return tryComplete(null);
    }

    private boolean tryFail0(Throwable cause) {
        synchronized (this) {
            if (succeeded || failed) {
                return false;
            }
            this.throwable = cause != null ? cause : new NoStackTraceThrowable(null);
            failed = true;
            notifyAll();
        }
        return true;
    }

    @Override
    public boolean tryFail(Throwable cause) {
        if (!tryFail0(cause)) {
            return false;
        }
        doAllHandlers();
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
    public Future<T> addHandler(Handler<AsyncResult<T>> handler) {
        boolean callHandler;
        synchronized (this) {
            if (handlers == null) {
                handlers = new LinkedList<>();
            }
            handlers.add(handler);
            callHandler = isComplete();
        }
        if (callHandler) {
            doAllHandlers();
        }
        return this;
    }

    @Override
    public Future<T> removeHandler(Handler<AsyncResult<T>> handler) {
        synchronized (this) {
            if (handlers != null) {
                handlers.remove(handler);
            }
        }
        return this;
    }

    @Override
    public Future<T> sync() throws Exception {
        return sync(0);
    }

    @Override
    public Future<T> sync(long timeout) throws Exception {
        CoreXImpl.ensureBlockSafe();

        boolean doHandlers;
        synchronized (this) {
            if (failed || succeeded) {
                return this;
            } else {
                try {
                    if (timeout <= 0) {
                        wait();
                    } else {
                        wait(timeout);
                    }
                } catch (Throwable e) {
                    failed = true;
                    throwable = e;
                }
                doHandlers = tryFail(new TimeoutException());
            }
        }

        if (failed) {
            if (throwable instanceof Exception) {
                throw (Exception) throwable;
            } else {
                throw new CoreException(throwable);
            }
        }

        if (doHandlers) {
            doAllHandlers();
        }

        return this;
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

    private void doAllHandlers() {
        List<Handler<AsyncResult<T>>> hs;
        synchronized (this) {
            hs = handlers;
            handlers = null;
        }

        if (hs == null) {
            return;
        }

        for (Handler<AsyncResult<T>> h : hs) {
            doHandler(h);
        }
    }
}
