package io.bigoldbro.corex.module;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
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
    Map<String, String> info();

    @Api(value = "kick", type = AUTH_TYPE_ADMIN)
    Future<Map<String, String>> kick(List<String> userIds, int code, String msg);

    @Api(value = "push", type = AUTH_TYPE_ADMIN)
    void push(List<String> channels, List<String> userIds, String topic, String msg);
}
