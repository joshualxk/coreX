package io.bigoldbro.corex.module;

import io.bigoldbro.corex.annotation.Module;

import java.util.Map;

/**
 * Created by Joshua on 2019/04/17.
 */
@Module(address = "dummy")
public interface DummyModule {

    Map<String, String> info();
}
