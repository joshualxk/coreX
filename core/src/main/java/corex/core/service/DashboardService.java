package corex.core.service;

import corex.core.FutureMo;
import corex.core.impl.CoreXImpl;
import corex.core.utils.CoreXUtil;
import corex.module.DashboardModule;
import corex.proto.ModelProto;

import java.util.List;

/**
 * Created by Joshua on 2018/3/1.
 */
public class DashboardService extends SimpleModuleService implements DashboardModule {

    @Override
    public FutureMo info() {
        return ((CoreXImpl) coreX()).info();
    }

    @Override
    public FutureMo kick(List<String> userIds, int code, String msg) {
        coreX().broadcast().kick(userIds, code, msg);

        return FutureMo.futureMo();
    }

    @Override
    public FutureMo push(List<String> channels, List<String> userIds, String topic, String msg) {
        FutureMo mo = FutureMo.futureMo();
        mo.putString("msg", msg);
        ModelProto.Broadcast broadcast = CoreXUtil.externalBroadcast(channels, userIds, topic, mo.toBodyHolder());
        coreX().broadcastMessage(broadcast);

        return FutureMo.futureMo();
    }

}
