package io.bigoldbro.corex.service;

import io.bigoldbro.corex.module.LogModule;

/**
 * Created by Joshua on 2018/3/1.
 */
public class LogService extends SimpleModuleService implements LogModule {

    @Override
    public void recordUserLogin(String userId, String channelId) {
        System.out.println("recordUserLogin");
    }

    @Override
    public void recordUserLogout(String userId, String channelId) {
        System.out.println("recordUserLogout");
    }

}
