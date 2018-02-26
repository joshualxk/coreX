package corex.demo;

import corex.core.FutureMo;
import corex.core.annotation.Api;
import corex.core.annotation.Module;
import corex.core.annotation.Param;
import corex.core.define.ConstDefine;

/**
 * Created by Joshua on 2018/3/26.
 */
@Module(address = "demo")
public interface DemoGame {

    @Api(value = "connect", type = ConstDefine.AUTH_TYPE_NON)
    FutureMo connect();

    @Api("match")
    FutureMo match(@Param("1") int type);

    @Api("leave")
    FutureMo leave();

    @Api("prepare")
    FutureMo prepare(@Param("1") boolean prepared);

    @Api("play")
    FutureMo play(@Param("1") int sjb);

}
