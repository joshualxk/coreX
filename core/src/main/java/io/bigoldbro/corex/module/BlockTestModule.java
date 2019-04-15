package io.bigoldbro.corex.module;

import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.BlockControl;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.define.ConstDefine;

import java.util.Map;

/**
 * Created by Joshua on 2018/3/29.
 */
@Module(address = "blockTest", bc = BlockControl.MULTI_THREADED)
public interface BlockTestModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    Map<String, String> info();

    @Api(value = "block", type = ConstDefine.AUTH_TYPE_NON)
    Map<String, String> block(int seconds);

}
