package io.bigoldbro.corex.module;

import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.BlockControl;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;

import java.util.Map;

/**
 * Created by Joshua on 2018/3/30.
 */
@Module(address = ServiceNameDefine.LOG, bc = BlockControl.MULTI_THREADED)
public interface LogModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    Map<String, String> info();

    @Api(value = "rul", type = ConstDefine.AUTH_TYPE_INTERNAL)
    void recordUserLogin(String userId, String channelId);

    @Api(value = "rul2", type = ConstDefine.AUTH_TYPE_INTERNAL)
    void recordUserLogout(String userId, String channelId);
}
