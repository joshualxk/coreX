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
 * Created by Joshua on 2018/4/4.
 */
@Module(address = ServiceNameDefine.CACHE, bc = BlockControl.MULTI_THREADED)
public interface CacheModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    Callback<JsonObject> info();

    @Api(value = "uc", type = ConstDefine.AUTH_TYPE_ADMIN)
    void updateCache();

    @Api(value = "gc", type = ConstDefine.AUTH_TYPE_INTERNAL)
    void getCache(@Param("1") String name);
}
