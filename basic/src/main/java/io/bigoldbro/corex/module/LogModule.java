package io.bigoldbro.corex.module;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.BlockControl;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;
import io.bigoldbro.corex.json.JsonObject;

/**
 * Created by Joshua on 2018/3/30.
 */
@Module(address = ServiceNameDefine.LOG, bc = BlockControl.MULTI_THREADED)
public interface LogModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    Callback<JsonObject> info();

    @Api(value = "rul", type = ConstDefine.AUTH_TYPE_INTERNAL)
    void recordUserLogin(@Param("1") String userId, @Param("2") String channelId);

    @Api(value = "rul2", type = ConstDefine.AUTH_TYPE_INTERNAL)
    void recordUserLogout(@Param("1") String userId, @Param("2") String channelId);
}
