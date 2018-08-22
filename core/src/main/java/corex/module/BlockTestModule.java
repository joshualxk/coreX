package corex.module;

import corex.core.JoHolder;
import corex.core.annotation.Api;
import corex.core.annotation.Module;
import corex.core.annotation.Param;
import corex.core.define.ConstDefine;
import corex.core.rpc.BlockControl;

/**
 * Created by Joshua on 2018/3/29.
 */
@Module(address = "blockTest", bc = BlockControl.MULTI_THREADED)
public interface BlockTestModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    JoHolder info();

    @Api(value = "block", type = ConstDefine.AUTH_TYPE_NON)
    JoHolder block(@Param("1") int seconds);

}
