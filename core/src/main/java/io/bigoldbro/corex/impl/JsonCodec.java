package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.Codec;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.model.ClientPayload;
import io.bigoldbro.corex.model.Payload;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Joshua on 2018/2/24.
 */
public class JsonCodec implements Codec {

    @Override
    public Payload readPayload(InputStream is) throws Exception {
        JsonObjectImpl jo = new JsonObjectImpl(is);
        return Payload.fromJo(jo);
    }

    @Override
    public void writePayload(OutputStream os, Payload payload) throws Exception {
        payload.toJo().encode(os);
    }

    @Override
    public ClientPayload readClientPayload(InputStream is) throws Exception {
        JsonObjectImpl jo = new JsonObjectImpl(is);
        return ClientPayload.fromJo(jo);
    }

    @Override
    public void writeClientPayload(OutputStream os, ClientPayload payload) throws Exception {
        payload.toJo().encode(os);
    }
}