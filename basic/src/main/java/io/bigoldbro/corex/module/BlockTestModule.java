package io.bigoldbro.corex.module;

import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.BlockControl;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.define.ConstDefine;

/**
 * Created by Joshua on 2018/3/29.
 */
@Module(address = "blockTest", bc = BlockControl.MULTI_THREADED)
public interface BlockTestModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    void info();

    @Api(value = "block", type = ConstDefine.AUTH_TYPE_NON)
    void block(@Param("1") int seconds);

}
