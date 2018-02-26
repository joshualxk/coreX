package corex.core;

import corex.proto.ModelProto;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Joshua on 2018/2/27.
 */
public interface Codec {

    ModelProto.Payload readPayload(InputStream inputStream) throws Exception;

    void writePayload(OutputStream outputStream, ModelProto.Payload payload) throws Exception;

    ModelProto.ClientPayload readClientPayload(InputStream inputStream) throws Exception;

    void writeClientPayload(OutputStream outputStream, ModelProto.ClientPayload payload) throws Exception;
}
