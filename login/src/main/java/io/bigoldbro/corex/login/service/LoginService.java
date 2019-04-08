package io.bigoldbro.corex.login.service;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.impl.SucceededCallback;
import io.bigoldbro.corex.json.JsonObject;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.service.SimpleModuleService;
import io.bigoldbro.corex.module.LoginModule;

import java.util.List;

/**
 * Created by Joshua on 2018/3/15.
 */
public class LoginService extends SimpleModuleService implements LoginModule {

    @Override
    public Callback<JsonObject> authorize(String token) {
        JsonObject jsonObject = new JsonObjectImpl();
        jsonObject.put("userId", "token" + token);
        return new SucceededCallback<>(jsonObject);
    }

    @Override
    public void getUser(String userId) {
    }

}
