package corex.module;

import corex.core.FutureMo;
import corex.core.annotation.Api;
import corex.core.annotation.Module;
import corex.core.annotation.Param;
import corex.core.define.ConstDefine;
import corex.core.define.ServiceNameDefine;
import corex.core.rpc.BlockControl;

/**
 * Created by Joshua on 2018/3/30.
 */
@Module(address = ServiceNameDefine.LOG, bc = BlockControl.MULTI_THREADED)
public interface LogModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    FutureMo info();

    @Api(value = "rul", type = ConstDefine.AUTH_TYPE_INTERNAL)
    void recordUserLogin(@Param("1") String userId, @Param("2") String channelId);

    @Api(value = "rul2", type = ConstDefine.AUTH_TYPE_INTERNAL)
    void recordUserLogout(@Param("1") String userId, @Param("2") String channelId);
}
