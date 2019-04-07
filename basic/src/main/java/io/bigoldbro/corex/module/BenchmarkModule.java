package io.bigoldbro.corex.module;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.BlockControl;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.define.ConstDefine;

/**
 * Created by Joshua on 2018/4/2.
 */
@Module(address = "benchmark", bc = BlockControl.NON_BLOCK)
public interface BenchmarkModule {

    @Api(value = "connect", type = ConstDefine.AUTH_TYPE_NON)
    Callback<Integer> connect(@Param("1") String msg);
}
