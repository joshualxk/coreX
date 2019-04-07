package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.Callback;

/**
 * Created by Joshua on 2019/4/8
 */
public class FailedCallback<T> extends FailedFuture<T> implements Callback<T> {

    public FailedCallback(Throwable t) {
        super(t);
    }
}
