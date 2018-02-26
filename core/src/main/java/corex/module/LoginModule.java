package corex.module;

import corex.core.FutureMo;
import corex.core.annotation.Api;
import corex.core.annotation.Module;
import corex.core.annotation.Param;
import corex.core.define.ConstDefine;
import corex.core.define.ServiceNameDefine;

import java.util.List;

import static corex.core.define.ConstDefine.AUTH_TYPE_INTERNAL;

/**
 * Created by Joshua on 2018/3/15.
 */
@Module(address = ServiceNameDefine.LOGIN)
public interface LoginModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    FutureMo info();

    @Api(value = "au", type = AUTH_TYPE_INTERNAL)
    FutureMo authorize(@Param("1") String token);

    @Api(value = "gu", type = AUTH_TYPE_INTERNAL)
    FutureMo getUser(@Param("1") String userId);

    @Api(value = "pay", type = AUTH_TYPE_INTERNAL)
    FutureMo pay(@Param("1") String userId, @Param("2") String tradeNo, @Param("3") List<Integer> types, @Param("4") List<Integer> amounts, @Param("5") boolean income, @Param("6") String itemEvent, @Param("7") String comments);

    @Api(value = "pam", type = AUTH_TYPE_INTERNAL)
    FutureMo pushAppMsg(@Param("1") List<String> userIds, @Param("2") String title, @Param("3") String info, @Param("4") String url);

    @Api(value = "qua", type = AUTH_TYPE_INTERNAL)
    FutureMo queryUserAgent(@Param("1") String userId);
}
