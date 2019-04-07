package io.bigoldbro.corex;

import io.bigoldbro.corex.impl.FailedFuture;
import io.bigoldbro.corex.impl.FutureImpl;
import io.bigoldbro.corex.impl.SucceededFuture;

/**
 * Created by Joshua on 2018/2/26.
 */
public interface Future<T> extends AsyncResult<T>, Handler<AsyncResult<T>> {

    static <T> Future<T> future() {
        return new FutureImpl<>();
    }

    static <T> Future<T> succeededFuture(T t) {
        return new SucceededFuture<>(t);
    }

    static <T> Future<T> succeededFuture() {
        return new SucceededFuture<>();
    }

    static <T> Future<T> failedFuture(String failureMessage) {
        return new FailedFuture<>(failureMessage);
    }

    static <T> Future<T> failedFuture(Throwable cause) {
        return new FailedFuture<>(cause);
    }

    void complete(T result);

    void complete();

    void fail(Throwable cause);

    void fail(String failureMessage);

    boolean tryComplete(T result);

    boolean tryComplete();

    boolean tryFail(Throwable cause);

    boolean tryFail(String failureMessage);

    boolean isComplete();

    Future<T> setHandler(Handler<AsyncResult<T>> handler);

    T sync();
}
