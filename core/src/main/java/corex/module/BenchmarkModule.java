package corex.module;

import corex.core.FutureMo;
import corex.core.annotation.Api;
import corex.core.annotation.Module;
import corex.core.annotation.Param;
import corex.core.define.ConstDefine;
import corex.core.rpc.BlockControl;

/**
 * Created by Joshua on 2018/4/2.
 */
@Module(address = "benchmark", bc = BlockControl.NON_BLOCK)
public interface BenchmarkModule {

    @Api(value = "connect", type = ConstDefine.AUTH_TYPE_NON)
    FutureMo connect(@Param("1") String msg);
}
