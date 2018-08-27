package corex.demo;

import corex.core.JoHolder;
import corex.core.annotation.Api;
import corex.core.annotation.Module;
import corex.core.annotation.Param;
import corex.core.define.ConstDefine;
import corex.core.json.JsonObject;

/**
 * Created by Joshua on 2018/3/26.
 */
@Module(address = "demo")
public interface DemoGame {

    @Api(value = "connect", type = ConstDefine.AUTH_TYPE_NON)
    JoHolder connect();

    @Api("match")
    JoHolder match();

    @Api("cancelMatch")
    JoHolder cancelMatch();

    @Api("play")
    JoHolder play(@Param("1") JsonObject op);

}
