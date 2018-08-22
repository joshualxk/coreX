package corex.core.service;

import corex.core.JoHolder;
import corex.core.impl.AsyncJoHolder;
import corex.core.json.JsonObject;
import corex.module.TestModule;

import java.util.concurrent.TimeUnit;

/**
 * Created by Joshua on 2018/3/1.
 */
public class TestService extends SimpleModuleService implements TestModule {

    @Override
    public JoHolder async(int delaySeconds) {
        AsyncJoHolder ret = JoHolder.newAsync();
        long start = System.currentTimeMillis();
        coreX().setTimer(Math.max(1000, TimeUnit.SECONDS.toMillis(delaySeconds)), tid -> {
            JoHolder ret2 = JoHolder.newSync();
            JsonObject jo = ret2.jo();
            jo.put("start", start);
            jo.put("end", System.currentTimeMillis());
            ret.complete(ret2);
        });
        return ret;
    }

    @Override
    public JoHolder async2(int delaySeconds) {
        AsyncJoHolder ret = JoHolder.newAsync();

        context.executeBlocking(fut -> {
            JoHolder ret2 = JoHolder.newSync();
            JsonObject jo = ret2.jo();
            long start = System.currentTimeMillis();
            jo.put("start", start);
            jo.put("thread", Thread.currentThread().getName());

            try {
                TimeUnit.SECONDS.sleep(delaySeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            jo.put("end", System.currentTimeMillis());
            ret.complete(ret2);
        }, false, null);
        return ret;
    }
}
