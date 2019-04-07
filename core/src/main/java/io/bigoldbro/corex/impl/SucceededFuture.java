package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.AsyncResult;
import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.Handler;

/**
 * Created by Joshua on 2018/2/26.
 */
public class SucceededFuture<T> implements Future<T> {

    private final T result;

    public SucceededFuture(T result) {
        this.result = result;
    }

    public SucceededFuture() {
        this.result = null;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
        handler.handle(this);
        return this;
    }

    @Override
    public void complete(T result) {
        throw new IllegalStateException("Result is already complete: succeeded");
    }

    @Override
    public void complete() {
        throw new IllegalStateException("Result is already complete: succeeded");
    }

    @Override
    public void fail(Throwable cause) {
        throw new IllegalStateException("Result is already complete: succeeded");
    }

    @Override
    public void fail(String failureMessage) {
        throw new IllegalStateException("Result is already complete: succeeded");
    }

    @Override
    public boolean tryComplete(T result) {
        return false;
    }

    @Override
    public boolean tryComplete() {
        return false;
    }

    @Override
    public boolean tryFail(Throwable cause) {
        return false;
    }

    @Override
    public boolean tryFail(String failureMessage) {
        return false;
    }

    @Override
    public T result() {
        return result;
    }

    @Override
    public Throwable cause() {
        return null;
    }

    @Override
    public boolean succeeded() {
        return true;
    }

    @Override
    public boolean failed() {
        return false;
    }

    @Override
    public void handle(AsyncResult<T> asyncResult) {
        throw new IllegalStateException("Result is already complete: succeeded");
    }

    @Override
    public String toString() {
        return "Future{result=" + result + "}";
    }
}
