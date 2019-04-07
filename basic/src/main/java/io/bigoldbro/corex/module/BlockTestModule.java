package io.bigoldbro.corex.module;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.BlockControl;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.json.JsonObject;

/**
 * Created by Joshua on 2018/3/29.
 */
@Module(address = "blockTest", bc = BlockControl.MULTI_THREADED)
public interface BlockTestModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    Callback<JsonObject> info();

    @Api(value = "block", type = ConstDefine.AUTH_TYPE_NON)
    Callback<JsonObject> block(@Param("1") int seconds);

}
