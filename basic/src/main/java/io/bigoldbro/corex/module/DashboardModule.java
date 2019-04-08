package io.bigoldbro.corex.module;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.define.ServiceNameDefine;
import io.bigoldbro.corex.json.JsonObject;

import java.util.List;

import static io.bigoldbro.corex.define.ConstDefine.AUTH_TYPE_ADMIN;

/**
 * Created by Joshua on 2018/3/29.
 */
@Module(address = ServiceNameDefine.DASHBOARD)
public interface DashboardModule {

    @Api(value = "i", type = AUTH_TYPE_ADMIN)
    Callback<JsonObject> info();

    @Api(value = "kick", type = AUTH_TYPE_ADMIN)
    Callback<Void> kick(@Param("1") List<String> userIds, @Param("2") int code, @Param("3") String msg);

    @Api(value = "push", type = AUTH_TYPE_ADMIN)
    Callback<Void> push(@Param("1") List<String> channels, @Param("2") List<String> userIds, @Param("3") String topic, @Param("4") String msg);
}
