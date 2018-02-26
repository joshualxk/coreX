package corex.module;

import corex.core.FutureMo;
import corex.core.annotation.Api;
import corex.core.annotation.Module;
import corex.core.annotation.Param;
import corex.core.define.ServiceNameDefine;

import java.util.List;

import static corex.core.define.ConstDefine.AUTH_TYPE_ADMIN;

/**
 * Created by Joshua on 2018/3/29.
 */
@Module(address = ServiceNameDefine.DASHBOARD)
public interface DashboardModule {

    @Api(value = "i", type = AUTH_TYPE_ADMIN)
    FutureMo info();

    @Api(value = "kick", type = AUTH_TYPE_ADMIN)
    FutureMo kick(@Param("1") List<String> userIds, @Param("2") int code, @Param("3") String msg);

    @Api(value = "push", type = AUTH_TYPE_ADMIN)
    FutureMo push(@Param("1") List<String> channels, @Param("2") List<String> userIds, @Param("3") String topic, @Param("4") String msg);
}
