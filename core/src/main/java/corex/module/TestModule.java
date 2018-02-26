package corex.module;

import corex.core.FutureMo;
import corex.core.annotation.Api;
import corex.core.annotation.Module;
import corex.core.annotation.Param;
import corex.core.define.ConstDefine;

/**
 * Created by Joshua on 2018/3/29.
 */
@Module(address = "test")
public interface TestModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    FutureMo info();

    @Api(value = "async", type = ConstDefine.AUTH_TYPE_NON)
    FutureMo async(@Param("1") int delaySeconds);

    @Api(value = "async2", type = ConstDefine.AUTH_TYPE_NON)
    FutureMo async2(@Param("1") int delaySeconds);
}
