package corex.core;

import corex.core.impl.JsonCodec;
import corex.core.model.ClientPayload;
import corex.core.model.Payload;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Joshua on 2018/2/27.
 */
public interface Codec {

    Payload readPayload(InputStream is) throws Exception;

    void writePayload(OutputStream os, Payload payload) throws Exception;

    ClientPayload readClientPayload(InputStream is) throws Exception;

    void writeClientPayload(OutputStream os, ClientPayload payload) throws Exception;

    static Codec defaultCodec() {
        return new JsonCodec();
    }
}
