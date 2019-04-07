package io.bigoldbro.corex.service;

import io.bigoldbro.corex.impl.SyncCallback;
import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.json.JsonObject;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.model.Broadcast;
import io.bigoldbro.corex.module.BenchmarkModule;

/**
 * Created by Joshua on 2018/4/2.
 */
public class BenchmarkService extends SimpleModuleService implements BenchmarkModule {

    @Override
    public Callback<Integer> connect(String msg) {
        JsonObject b = new JsonObjectImpl();
        b.put("msg", msg);
        Broadcast broadcast = Broadcast.newGroupBroadcast("benchmark-channel", null, "haha", b);
        coreX().broadcastMessage(broadcast);

        return new SyncCallback<>(1);
    }

}
