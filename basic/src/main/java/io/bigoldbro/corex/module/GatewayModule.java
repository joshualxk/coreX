package io.bigoldbro.corex.module;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;
import io.bigoldbro.corex.json.JsonObject;

/**
 * Created by Joshua on 2018/3/29.
 */
@Module(address = ServiceNameDefine.GATEWAY)
public interface GatewayModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    Callback<JsonObject> info();
}
