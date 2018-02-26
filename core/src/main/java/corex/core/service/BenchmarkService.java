package corex.core.service;

import corex.core.FutureMo;
import corex.core.utils.CoreXUtil;
import corex.module.BenchmarkModule;
import corex.proto.ModelProto;

/**
 * Created by Joshua on 2018/4/2.
 */
public class BenchmarkService extends SimpleModuleService implements BenchmarkModule {

    @Override
    public FutureMo connect(String msg) {
        FutureMo b = FutureMo.futureMo();
        b.putString("msg", msg);
        ModelProto.Broadcast broadcast = CoreXUtil.externalBroadcast("benchmark-channel", null, "haha", b.toBodyHolder());
        coreX().broadcastMessage(broadcast);

        FutureMo futureMo = FutureMo.futureMo();
        return futureMo;
    }
}
