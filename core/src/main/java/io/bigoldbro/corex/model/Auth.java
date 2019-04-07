package io.bigoldbro.corex.model;

import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.json.Joable;
import io.bigoldbro.corex.json.JsonObject;

/**
 * Created by Joshua on 2018/8/22
 */
public class Auth implements Joable {

    private int type;
    private String token;

    private Auth(int type, String token) {
        this.type = type;
        this.token = token;
    }

    public Auth() {
    }

    public static Auth newAuth(int type, String token) {
        return new Auth(type, token);
    }

    public static Auth internalAuth() {
        return new Auth(ConstDefine.AUTH_TYPE_INTERNAL, "");
    }

    public static Auth adminAuth() {
        return new Auth(ConstDefine.AUTH_TYPE_ADMIN, "");
    }

    public int getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public void readFrom(JsonObject jo) {
        type = jo.getInteger("t");
        token = jo.getString("to");
    }

    @Override
    public void writeTo(JsonObject jo) {
        jo.put("t", type)
                .put("to", token);
    }
}
