package corex.core.service;

import corex.core.JoHolder;
import corex.core.json.JsonObject;
import corex.core.model.Broadcast;
import corex.module.BenchmarkModule;

/**
 * Created by Joshua on 2018/4/2.
 */
public class BenchmarkService extends SimpleModuleService implements BenchmarkModule {

    @Override
    public JoHolder connect(String msg) {
        JsonObject b = new JsonObject();
        b.put("msg", msg);
        Broadcast broadcast = Broadcast.newExternalBroadcast("benchmark-channel", null, "haha", b);
        coreX().broadcastMessage(broadcast);

        JoHolder ret = JoHolder.newSync();
        return ret;
    }

}
