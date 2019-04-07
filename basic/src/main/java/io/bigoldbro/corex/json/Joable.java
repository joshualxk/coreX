package io.bigoldbro.corex.json;

import io.bigoldbro.corex.exception.DecodeException;

/**
 * Created by Joshua on 2019/4/7
 */
public interface Joable {

    void readFrom(JsonObject jo) throws DecodeException;

    void writeTo(JsonObject jo);
}
