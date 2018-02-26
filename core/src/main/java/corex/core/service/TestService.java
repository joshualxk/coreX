package corex.core.service;

import corex.core.AsyncFutureMo;
import corex.core.FutureMo;
import corex.module.TestModule;

import java.util.concurrent.TimeUnit;

/**
 * Created by Joshua on 2018/3/1.
 */
public class TestService extends SimpleModuleService implements TestModule {

    @Override
    public FutureMo info() {
        FutureMo ret = baseInfo();
        return ret;
    }

    @Override
    public FutureMo async(int delaySeconds) {
        AsyncFutureMo asyncFutureMo = FutureMo.asyncFutureMo();
        long start = System.currentTimeMillis();
        coreX().setTimer(Math.max(1000, TimeUnit.SECONDS.toMillis(delaySeconds)), tid -> {
            FutureMo futureMo = FutureMo.futureMo();
            futureMo.putLong("start", start);
            futureMo.putLong("end", System.currentTimeMillis());
            asyncFutureMo.complete(futureMo);
        });
        return asyncFutureMo;
    }

    @Override
    public FutureMo async2(int delaySeconds) {
        AsyncFutureMo asyncFutureMo = FutureMo.asyncFutureMo();

        context.executeBlocking(fut -> {
            FutureMo futureMo = FutureMo.futureMo();
            long start = System.currentTimeMillis();
            futureMo.putLong("start", start);
            futureMo.putString("thread", Thread.currentThread().getName());

            try {
                TimeUnit.SECONDS.sleep(delaySeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            futureMo.putLong("end", System.currentTimeMillis());
            asyncFutureMo.complete(futureMo);
        }, false, null);
        return asyncFutureMo;
    }
}
