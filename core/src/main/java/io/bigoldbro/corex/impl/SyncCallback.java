package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.AsyncResult;
import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.Handler;

/**
 * Created by Joshua on 2019/4/7
 */
public class SyncCallback<T> implements Callback<T> {

    private final T body;

    public SyncCallback(T body) {
        this.body = body;
    }

    @Override
    public T sync() throws Exception {
        return body;
    }

    @Override
    public void onResult(Handler<AsyncResult<T>> handler) {
        throw new UnsupportedOperationException("onResult");
    }
}
