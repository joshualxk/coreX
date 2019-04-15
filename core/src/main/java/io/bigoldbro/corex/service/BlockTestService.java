package io.bigoldbro.corex.service;

import io.bigoldbro.corex.module.BlockTestModule;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Joshua on 2018/3/1.
 */
public class BlockTestService extends SimpleModuleService implements BlockTestModule {

    @Override
    public Map<String, String> block(int seconds) {
        Map<String, String> map = new HashMap<>();
        map.put("thread", Thread.currentThread().getName());
        map.put("startTime", String.valueOf(System.currentTimeMillis()));

        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        map.put("endTime", String.valueOf(System.currentTimeMillis()));

        return map;
    }

}
