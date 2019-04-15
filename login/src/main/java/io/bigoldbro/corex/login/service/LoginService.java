package io.bigoldbro.corex.login.service;

import io.bigoldbro.corex.module.LoginModule;
import io.bigoldbro.corex.service.SimpleModuleService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joshua on 2018/3/15.
 */
public class LoginService extends SimpleModuleService implements LoginModule {

    @Override
    public Map<String, String> authorize(String token) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", "token" + token);
        return map;
    }

    @Override
    public void getUser(String userId) {
    }

}
