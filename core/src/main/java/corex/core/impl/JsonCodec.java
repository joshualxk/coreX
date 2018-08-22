package corex.core.impl;

import corex.core.Codec;
import corex.core.json.JsonObject;
import corex.core.model.ClientPayload;
import corex.core.model.Payload;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Joshua on 2018/2/24.
 */
public class JsonCodec implements Codec {

    @Override
    public Payload readPayload(InputStream is) throws Exception {
        JsonObject jo = new JsonObject(is);
        return Payload.fromJo(jo);
    }

    @Override
    public void writePayload(OutputStream os, Payload payload) throws Exception {
        payload.toJo().encode(os);
    }

    @Override
    public ClientPayload readClientPayload(InputStream is) throws Exception {
        JsonObject jo = new JsonObject(is);
        return ClientPayload.fromJo(jo);
    }

    @Override
    public void writeClientPayload(OutputStream os, ClientPayload payload) throws Exception {
        payload.toJo().encode(os);
    }
}
