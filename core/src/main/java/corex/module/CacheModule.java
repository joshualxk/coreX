package corex.module;

import corex.core.JoHolder;
import corex.core.annotation.Api;
import corex.core.annotation.Module;
import corex.core.annotation.Param;
import corex.core.define.ConstDefine;
import corex.core.define.ServiceNameDefine;
import corex.core.rpc.BlockControl;

/**
 * Created by Joshua on 2018/4/4.
 */
@Module(address = ServiceNameDefine.CACHE, bc = BlockControl.MULTI_THREADED)
public interface CacheModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    JoHolder info();

    @Api(value = "uc", type = ConstDefine.AUTH_TYPE_ADMIN)
    JoHolder updateCache();

    @Api(value = "gc", type = ConstDefine.AUTH_TYPE_INTERNAL)
    JoHolder getCache(@Param("1") String name);
}
