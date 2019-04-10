package io.bigoldbro.corex.module;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.define.ServiceNameDefine;

import java.util.List;
import java.util.Map;

import static io.bigoldbro.corex.define.ConstDefine.AUTH_TYPE_ADMIN;

/**
 * Created by Joshua on 2018/3/29.
 */
@Module(address = ServiceNameDefine.DASHBOARD)
public interface DashboardModule {

    @Api(value = "i", type = AUTH_TYPE_ADMIN)
    Map<String, Object> info();

    @Api(value = "kick", type = AUTH_TYPE_ADMIN)
    Callback<Void> kick(@Param("1") List<String> userIds, @Param("2") int code, @Param("3") String msg);

    @Api(value = "push", type = AUTH_TYPE_ADMIN)
    Callback<Void> push(@Param("1") List<String> channels, @Param("2") List<String> userIds, @Param("3") String topic, @Param("4") String msg);
}
