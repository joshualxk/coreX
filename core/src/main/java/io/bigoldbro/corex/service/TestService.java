package io.bigoldbro.corex.service;

import io.bigoldbro.corex.module.TestModule;

/**
 * Created by Joshua on 2018/3/1.
 */
public class TestService extends SimpleModuleService implements TestModule {

    @Override
    public void async(int delaySeconds) {
    }

    @Override
    public void async2(int delaySeconds) {
    }
}
