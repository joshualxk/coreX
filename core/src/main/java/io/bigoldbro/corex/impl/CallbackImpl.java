package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.Context;
import io.bigoldbro.corex.exception.CoreException;

/**
 * Created by Joshua on 2019/4/8
 */
public class CallbackImpl<T> extends FutureImpl<T> implements Callback<T> {

    @Override
    public T sync() {
        Context context = CoreXImpl.getContext();
        if (context != null && !context.isWorker()) {
            throw new CoreException("io线程不能阻塞!");
        }
        return super.sync();
    }
}
