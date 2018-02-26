package corex.core.impl;

import corex.core.Codec;
import corex.proto.ModelProto.ClientPayload;
import corex.proto.ModelProto.Payload;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Joshua on 2018/2/24.
 */
public class ProtobufCodec implements Codec {

    @Override
    public Payload readPayload(InputStream inputStream) throws Exception {
        return Payload.parseFrom(inputStream);
    }

    @Override
    public void writePayload(OutputStream outputStream, Payload payload) throws Exception {
        payload.writeTo(outputStream);
    }

    @Override
    public ClientPayload readClientPayload(InputStream inputStream) throws Exception {
        return ClientPayload.parseFrom(inputStream);
    }

    @Override
    public void writeClientPayload(OutputStream outputStream, ClientPayload payload) throws Exception {
        payload.writeTo(outputStream);
    }
}
