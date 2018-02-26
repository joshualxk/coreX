package corex.core.service;

import corex.core.FutureMo;
import corex.module.BlockTestModule;

import java.util.concurrent.TimeUnit;

/**
 * Created by Joshua on 2018/3/1.
 */
public class BlockTestService extends SimpleModuleService implements BlockTestModule {

    @Override
    public FutureMo info() {
        FutureMo ret = baseInfo();
        return ret;
    }

    @Override
    public FutureMo block(int seconds) {
        FutureMo ret = FutureMo.futureMo();
        ret.putString("thread", Thread.currentThread().getName());
        ret.putLong("startTime", System.currentTimeMillis());

        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ret.putLong("endTime", System.currentTimeMillis());

        return ret;
    }

}
