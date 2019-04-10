package io.bigoldbro.corex.module;

import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;

import java.util.Map;

import static io.bigoldbro.corex.define.ConstDefine.AUTH_TYPE_INTERNAL;

/**
 * Created by Joshua on 2018/3/15.
 */
@Module(address = ServiceNameDefine.LOGIN)
public interface LoginModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    Map<String, Object> info();

    @Api(value = "au", type = AUTH_TYPE_INTERNAL)
    Map<String, Object> authorize(@Param("1") String token);

    @Api(value = "gu", type = AUTH_TYPE_INTERNAL)
    void getUser(@Param("1") String userId);

}
