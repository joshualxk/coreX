package io.bigoldbro.corex.service;

import io.bigoldbro.corex.module.BenchmarkModule;

/**
 * Created by Joshua on 2018/4/2.
 */
public class BenchmarkService extends SimpleModuleService implements BenchmarkModule {

    @Override
    public int connect(String msg) {
        return 0;
    }

}
