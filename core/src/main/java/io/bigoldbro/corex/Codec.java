package io.bigoldbro.corex;

import io.bigoldbro.corex.impl.JsonCodec;
import io.bigoldbro.corex.proto.Base;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Joshua on 2018/2/27.
 */
public interface Codec {

    Base.Payload readPayload(InputStream is) throws Exception;

    void writePayload(OutputStream os, Base.Payload payload) throws Exception;

    Base.ClientPayload readClientPayload(InputStream is) throws Exception;

    void writeClientPayload(OutputStream os, Base.ClientPayload payload) throws Exception;

    static Codec defaultCodec() {
        return new JsonCodec();
    }
}
