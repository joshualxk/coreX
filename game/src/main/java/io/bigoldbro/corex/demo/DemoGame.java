package io.bigoldbro.corex.demo;

import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.json.JsonObjectImpl;

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
    JoHolder play(@Param("1") JsonObjectImpl op);

}
