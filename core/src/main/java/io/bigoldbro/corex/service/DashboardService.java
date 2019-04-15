package io.bigoldbro.corex.service;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.impl.CoreXImpl;
import io.bigoldbro.corex.module.DashboardModule;

import java.util.List;
import java.util.Map;

/**
 * Created by Joshua on 2018/3/1.
 */
public class DashboardService extends SimpleModuleService implements DashboardModule {

    @Override
    public Map<String, String> info() {
        return ((CoreXImpl) coreX()).info();
    }

    @Override
    public Future<Map<String, String>> kick(List<String> userIds, int code, String msg) {
        coreX().broadcast().kick(userIds, code, msg);
        return null;
    }

    @Override
    public void push(List<String> channels, List<String> userIds, String topic, String msg) {
//        JsonObjectImpl jo = new JsonObjectImpl();
//        jo.put("msg", msg);
//        Broadcast broadcast = Broadcast.newCsUsBroadcast(channels, userIds, topic, jo);
//        coreX().broadcastMessage(broadcast);
    }

}
