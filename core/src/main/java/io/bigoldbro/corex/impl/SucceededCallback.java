package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.Callback;

/**
 * Created by Joshua on 2019/4/8
 */
public class SucceededCallback<T> extends SucceededFuture<T> implements Callback<T> {

    public SucceededCallback(T result) {
        super(result);
    }

    public SucceededCallback() {
    }

    @Override
    public Callback<T> sync() {
        return this;
    }
}
