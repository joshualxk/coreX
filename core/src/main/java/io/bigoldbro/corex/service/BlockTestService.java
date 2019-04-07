package io.bigoldbro.corex.service;

import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.module.BlockTestModule;

import java.util.concurrent.TimeUnit;

/**
 * Created by Joshua on 2018/3/1.
 */
public class BlockTestService extends SimpleModuleService implements BlockTestModule {

    @Override
    public JoHolder block(int seconds) {
        JoHolder ret = JoHolder.newSync();
        JsonObjectImpl jo = ret.jo();
        jo.put("thread", Thread.currentThread().getName());
        jo.put("startTime", System.currentTimeMillis());

        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jo.put("endTime", System.currentTimeMillis());

        return ret;
    }

}
