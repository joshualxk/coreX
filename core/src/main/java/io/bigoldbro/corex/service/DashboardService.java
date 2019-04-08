package io.bigoldbro.corex.service;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.impl.CoreXImpl;
import io.bigoldbro.corex.impl.SucceededCallback;
import io.bigoldbro.corex.json.JsonObject;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.model.Broadcast;
import io.bigoldbro.corex.module.DashboardModule;

import java.util.List;

/**
 * Created by Joshua on 2018/3/1.
 */
public class DashboardService extends SimpleModuleService implements DashboardModule {

    @Override
    public Callback<JsonObject> info() {
        return ((CoreXImpl) coreX()).info();
    }

    @Override
    public Callback<Void> kick(List<String> userIds, int code, String msg) {
        coreX().broadcast().kick(userIds, code, msg);

        return new SucceededCallback<>();
    }

    @Override
    public Callback<Void> push(List<String> channels, List<String> userIds, String topic, String msg) {
        JsonObjectImpl jo = new JsonObjectImpl();
        jo.put("msg", msg);
        Broadcast broadcast = Broadcast.newCsUsBroadcast(channels, userIds, topic, jo);
        coreX().broadcastMessage(broadcast);

        return new SucceededCallback<>();
    }

}
