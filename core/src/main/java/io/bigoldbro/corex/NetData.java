package io.bigoldbro.corex;

import com.google.protobuf.ByteString;
import io.bigoldbro.corex.exception.CodecException;
import io.bigoldbro.corex.proto.Base;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;

import java.io.DataInput;
import java.io.DataOutput;

/**
 * Created by Joshua on 2019/04/15.
 */
public interface NetData {

    default Base.Body toBody() {
        ByteBuf byteBuffer = PooledByteBufAllocator.DEFAULT.buffer();
        try (ByteBufOutputStream os = new ByteBufOutputStream(byteBuffer)) {
            write(os);

            try (ByteBufInputStream is = new ByteBufInputStream(byteBuffer)) {
                return Base.Body.newBuilder()
                        .addFields(ByteString.readFrom(is))
                        .build();
            }
        } catch (Exception e) {
            throw new CodecException("编码错误");
        } finally {
            byteBuffer.release();
        }
    }

    void read(DataInput dataInput) throws Exception;

    void write(DataOutput dataOutput) throws Exception;
}
