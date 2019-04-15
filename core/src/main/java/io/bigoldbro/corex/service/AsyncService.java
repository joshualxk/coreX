package io.bigoldbro.corex.service;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.exception.BizException;
import io.bigoldbro.corex.impl.FutureImpl;
import io.bigoldbro.corex.module.AsyncModule;

/**
 * Created by Joshua on 2018/3/1.
 */
public class AsyncService extends SimpleModuleService implements AsyncModule {

    @Override
    public void start(Future<Void> completeFuture) {
        super.start(completeFuture);
    }

    @Override
    public Future<String> async(int delaySeconds) {
        Future<String> fut = new FutureImpl<>();

        if (delaySeconds < 0) {
            throw BizException.newException(-9999, "delaySeconds < 0");
        }
        coreX().setTimer(delaySeconds, tid -> {
            fut.complete("delaySeconds: " + delaySeconds);
        });
        return fut;
    }

    @Override
    public int sync(int val) {
        return val;
    }
}
