package io.bigoldbro.corex.json;

/**
 * Created by Joshua on 2019/4/7
 */
public interface Joable {

    void readFrom(JsonObject jo) throws Exception;

    void writeTo(JsonObject jo);
}
