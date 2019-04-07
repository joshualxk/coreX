package io.bigoldbro.corex.service;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.impl.SucceededCallback;
import io.bigoldbro.corex.json.JsonObject;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.module.BlockTestModule;

import java.util.concurrent.TimeUnit;

/**
 * Created by Joshua on 2018/3/1.
 */
public class BlockTestService extends SimpleModuleService implements BlockTestModule {

    @Override
    public Callback<JsonObject> block(int seconds) {
        JsonObject jo = new JsonObjectImpl();
        jo.put("thread", Thread.currentThread().getName());
        jo.put("startTime", System.currentTimeMillis());

        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jo.put("endTime", System.currentTimeMillis());

        return new SucceededCallback<>(jo);
    }

}
