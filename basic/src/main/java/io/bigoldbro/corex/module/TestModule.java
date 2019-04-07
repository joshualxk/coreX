package io.bigoldbro.corex.module;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.json.JsonObject;

/**
 * Created by Joshua on 2018/3/29.
 */
@Module(address = "test")
public interface TestModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    Callback<JsonObject> info();

    @Api(value = "async", type = ConstDefine.AUTH_TYPE_NON)
    void async(@Param("1") int delaySeconds);

    @Api(value = "async2", type = ConstDefine.AUTH_TYPE_NON)
    void async2(@Param("1") int delaySeconds);
}
