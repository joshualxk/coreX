package io.bigoldbro.corex.module;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;
import io.bigoldbro.corex.json.JsonObject;

import java.util.List;

import static io.bigoldbro.corex.define.ConstDefine.AUTH_TYPE_INTERNAL;

/**
 * Created by Joshua on 2018/3/15.
 */
@Module(address = ServiceNameDefine.LOGIN)
public interface LoginModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    Callback<JsonObject> info();

    @Api(value = "au", type = AUTH_TYPE_INTERNAL)
    Callback<JsonObject> authorize(@Param("1") String token);

    @Api(value = "gu", type = AUTH_TYPE_INTERNAL)
    void getUser(@Param("1") String userId);

}
