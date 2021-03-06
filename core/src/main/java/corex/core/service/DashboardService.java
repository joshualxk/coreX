package corex.core.service;

import corex.core.JoHolder;
import corex.core.impl.CoreXImpl;
import corex.core.json.JsonObject;
import corex.core.model.Broadcast;
import corex.module.DashboardModule;

import java.util.List;

/**
 * Created by Joshua on 2018/3/1.
 */
public class DashboardService extends SimpleModuleService implements DashboardModule {

    @Override
    public JoHolder info() {
        return ((CoreXImpl) coreX()).info();
    }

    @Override
    public JoHolder kick(List<String> userIds, int code, String msg) {
        coreX().broadcast().kick(userIds, code, msg);

        return JoHolder.newSync();
    }

    @Override
    public JoHolder push(List<String> channels, List<String> userIds, String topic, String msg) {
        JsonObject jo = new JsonObject();
        jo.put("msg", msg);
        Broadcast broadcast = Broadcast.newCsUsBroadcast(channels, userIds, topic, jo);
        coreX().broadcastMessage(broadcast);

        return JoHolder.newSync();
    }

}
