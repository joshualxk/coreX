package io.bigoldbro.corex.model;

import io.bigoldbro.corex.json.Joable;
import io.bigoldbro.corex.json.JsonObject;

/**
 * Created by Joshua on 2018/8/22
 */
public class Method implements Joable {

    private String module;
    private String api;
    private String version;

    private Method(String module, String api, String version) {
        this.module = module;
        this.api = api;
        this.version = version;
    }

    public Method() {
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

    public void readFrom(JsonObject jo) {
        module = jo.getString("m");
        api = jo.getString("a");
        version = jo.getString("v");
    }

    @Override
    public void writeTo(JsonObject jo) {
        jo.put("m", module)
                .put("a", api)
                .put("v", version);
    }
}
