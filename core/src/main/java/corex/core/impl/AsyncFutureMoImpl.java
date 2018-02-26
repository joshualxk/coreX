package corex.core.impl;

import corex.core.*;

/**
 * Created by Joshua on 2018/3/14.
 */
public class AsyncFutureMoImpl extends DummyFutureMo implements AsyncFutureMo {

    private final Future<FutureMo> future = Future.future();

    @Override
    public void complete(FutureMo result) {
        future.complete(result);
    }

    @Override
    public void complete() {
        future.complete();
    }

    @Override
    public void fail(Throwable cause) {
        future.fail(cause);
    }

    @Override
    public void fail(String failureMessage) {
        future.fail(failureMessage);
    }

    @Override
    public boolean tryComplete(FutureMo result) {
        return future.tryComplete(result);
    }

    @Override
    public boolean tryComplete() {
        return future.tryComplete();
    }

    @Override
    public boolean tryFail(Throwable cause) {
        return future.tryFail(cause);
    }

    @Override
    public boolean tryFail(String failureMessage) {
        return future.tryFail(failureMessage);
    }

    @Override
    public boolean isComplete() {
        return future.isComplete();
    }

    @Override
    public Future<FutureMo> setHandler(Handler<AsyncResult<FutureMo>> handler) {
        return future.setHandler(handler);
    }

    @Override
    public FutureMo result() {
        return future.result();
    }

    @Override
    public Throwable cause() {
        return future.cause();
    }

    @Override
    public boolean succeeded() {
        return future.succeeded();
    }

    @Override
    public boolean failed() {
        return future.failed();
    }

    @Override
    public void handle(AsyncResult<FutureMo> event) {
        future.handle(event);
    }
}
