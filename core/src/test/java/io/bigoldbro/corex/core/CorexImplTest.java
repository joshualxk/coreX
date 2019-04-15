package io.bigoldbro.corex.core;

import io.bigoldbro.corex.CoreX;
import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.impl.CoreXConfig;
import io.bigoldbro.corex.impl.CoreXImpl;
import io.bigoldbro.corex.impl.FutureImpl;
import io.bigoldbro.corex.module.DummyModule;
import io.bigoldbro.corex.service.SimpleModuleService;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Joshua on 2019/04/17.
 */
public class CorexImplTest {

    static AtomicInteger counter = new AtomicInteger(0);
    static long interval = 1000;

    @Test
    public void testStopSchedule() throws Exception {
        String path = "src/test/resources/testcorex.conf";
        CoreX corex = new CoreXImpl(CoreXConfig.readConfig(path));

        Future<String> future = new FutureImpl<>();
        corex.startService(ScheduleService.class, future);
        String name = future.sync().result();

        Future<Void> future1 = new FutureImpl<>();
        corex.stopService(name, future1);
        future1.sync();

        int oldVal = counter.get();

        Thread.sleep(2 * interval);

        int newVal = counter.get();

        Assert.assertEquals(oldVal, newVal);

    }

    public static class ScheduleService extends SimpleModuleService implements DummyModule {

        @Override
        public void start(Future<Void> completeFuture) {
            coreX().setPeriodic(interval, tid -> {
                counter.getAndIncrement();
            });
            super.start(completeFuture);
        }
    }
}
