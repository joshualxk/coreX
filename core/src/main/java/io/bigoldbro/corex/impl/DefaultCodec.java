package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.Codec;
import io.bigoldbro.corex.proto.Base;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Joshua on 2018/2/24.
 */
public class DefaultCodec implements Codec {

    @Override
    public Base.Payload readPayload(InputStream is) throws Exception {
        return Base.Payload.parseFrom(is);
    }

    @Override
    public void writePayload(OutputStream os, Base.Payload payload) throws Exception {
        payload.writeTo(os);
    }

    @Override
    public Base.ClientPayload readClientPayload(InputStream is) throws Exception {
        return Base.ClientPayload.parseFrom(is);
    }

    @Override
    public void writeClientPayload(OutputStream os, Base.ClientPayload payload) throws Exception {
        payload.writeTo(os);
    }
}
