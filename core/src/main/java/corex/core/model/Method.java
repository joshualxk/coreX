package corex.core.model;

import corex.core.Joable;
import corex.core.json.JsonObject;

/**
 * Created by Joshua on 2018/8/22
 */
public class Method implements Joable {

    private final String module;
    private final String api;
    private final String version;

    private Method(String module, String api, String version) {
        this.module = module;
        this.api = api;
        this.version = version;
    }

    public static Method newMethod(String module, String api, String version) {
        return new Method(module, api, version);
    }

    public String getModule() {
        return module;
    }

    public String getApi() {
        return api;
    }

    public String getVersion() {
        return version;
    }

    public static Method fromJo(JsonObject jo) {
        String module = jo.getString("m");
        String api = jo.getString("a");
        String version = jo.getString("v");
        return new Method(module, api, version);
    }

    @Override
    public JsonObject toJo() {
        return new JsonObject()
                .put("m", module)
                .put("a", api)
                .put("v", version);
    }
}
