package corex.core.model;

import corex.core.Joable;
import corex.core.define.ConstDefine;
import corex.core.json.JsonObject;

/**
 * Created by Joshua on 2018/8/22
 */
public class Auth implements Joable {

    private static final Auth INTERNAL_AUTH = new Auth(ConstDefine.AUTH_TYPE_INTERNAL, "");
    private static final Auth ADMIN_AUTH = new Auth(ConstDefine.AUTH_TYPE_ADMIN, "");

    private final int type;
    private final String token;

    private Auth(int type, String token) {
        this.type = type;
        this.token = token;
    }

    public static Auth newAuth(int type, String token) {
        return new Auth(type, token);
    }

    public static Auth internalAuth() {
        return INTERNAL_AUTH;
    }

    public static Auth adminAuth() {
        return ADMIN_AUTH;
    }

    public int getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public static Auth fromJo(JsonObject jo) throws Exception {
        int type = jo.getInteger("t");
        String token = jo.getString("to");
        return newAuth(type, token);
    }

    @Override
    public JsonObject toJo() {
        return new JsonObject()
                .put("t", type)
                .put("to", token);
    }
}
