package io.bigoldbro.corex.module;

import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;

import java.util.List;

import static io.bigoldbro.corex.define.ConstDefine.AUTH_TYPE_INTERNAL;

/**
 * Created by Joshua on 2018/3/15.
 */
@Module(address = ServiceNameDefine.LOGIN)
public interface LoginModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    void info();

    @Api(value = "au", type = AUTH_TYPE_INTERNAL)
    void authorize(@Param("1") String token);

    @Api(value = "gu", type = AUTH_TYPE_INTERNAL)
    void getUser(@Param("1") String userId);

    @Api(value = "pay", type = AUTH_TYPE_INTERNAL)
    void pay(@Param("1") String userId, @Param("2") String tradeNo, @Param("3") List<Integer> types, @Param("4") List<Integer> amounts, @Param("5") boolean income, @Param("6") String itemEvent, @Param("7") String comments);

    @Api(value = "pam", type = AUTH_TYPE_INTERNAL)
    void pushAppMsg(@Param("1") List<String> userIds, @Param("2") String title, @Param("3") String info, @Param("4") String url);

    @Api(value = "qua", type = AUTH_TYPE_INTERNAL)
    void queryUserAgent(@Param("1") String userId);
}
