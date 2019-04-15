package io.bigoldbro.corex.module;

import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;

import java.util.Map;

import static io.bigoldbro.corex.define.ConstDefine.AUTH_TYPE_INTERNAL;
import static io.bigoldbro.corex.define.ConstDefine.AUTH_TYPE_NON;

/**
 * Created by Joshua on 2018/3/15.
 */
@Module(address = ServiceNameDefine.LOGIN)
public interface LoginModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    Map<String, String> info();

    @Api(value = "au", type = AUTH_TYPE_NON)
    Map<String, String> authorize(String token);

    @Api(value = "gu", type = AUTH_TYPE_INTERNAL)
    void getUser(String userId);

}
