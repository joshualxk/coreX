package io.bigoldbro.corex.rpc;

import com.google.protobuf.ByteString;
import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.NetData;
import io.bigoldbro.corex.exception.CodecException;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.proto.Base;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;

import java.io.DataOutput;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

/**
 * Created by Joshua on 2018/3/8.
 */
class ClientRpcHandler implements RpcHandler {

    private final MethodDetail methodDetail;

    public ClientRpcHandler(MethodDetail methodDetail) {
        this.methodDetail = methodDetail;
    }

    @Override
    public Base.Body convert(Object[] args) {
        if ((args == null ? 0 : args.length) != methodDetail.params.size()) {
            throw new CoreException("参数数量不一致");
        }

        Base.Body.Builder builder = Base.Body.newBuilder();
        int i = 0;
        for (ParamDetail paramDetail : methodDetail.params) {
            Object arg = args[i];
            if (arg == null) {
                throw new CodecException("参数不能为空, index:" + i);
            }

            builder.addFields(wrapObj(paramDetail, arg));
            ++i;
        }
        return builder.build();
    }

    static void checkSize(int len) {
        if (len >= Short.MAX_VALUE || len < 0) {
            throw new CodecException("长度超出限制:" + len);
        }
    }

    static ByteString wrapObj(ParamDetail paramDetail, Object obj) {
        switch (paramDetail.type) {
            case UNSUPPORTED:
                throw new CoreException("不支持的类型");
            case LIST:
                return parseList(paramDetail, obj);
            case MAP:
                return parseMap(paramDetail, obj);
            case ARRAY:
                return parseArray(paramDetail, obj);
            default:
                return parseValue(paramDetail, obj);
        }
    }

    @SuppressWarnings("unchecked")
    private static ByteString parseList(ParamDetail paramDetail, Object obj) {
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();

        try (ByteBufOutputStream os = new ByteBufOutputStream(byteBuf)) {
            List list = (List) obj;
            int size = list.size();
            checkSize(size);

            // list size
            os.writeShort(size);
            for (Object elem : list) {
                if (elem == null) {
                    throw new CodecException("List元素不能为空");
                }

                writeValue(os, paramDetail.genericType, obj);
            }

            try (ByteBufInputStream is = new ByteBufInputStream(byteBuf)) {
                return ByteString.readFrom(is);
            }
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException(e);
        } finally {
            byteBuf.release();
        }

    }

    @SuppressWarnings("unchecked")
    private static ByteString parseMap(ParamDetail paramDetail, Object obj) {
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();

        try (ByteBufOutputStream os = new ByteBufOutputStream(byteBuf)) {
            Map<String, Object> map = (Map<String, Object>) obj;
            int size = map.size();
            checkSize(size);

            // list size
            os.writeShort(size);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                if (k == null || v == null) {
                    throw new CodecException("Map元素不能为空");
                }
                os.writeUTF(k);
                writeValue(os, paramDetail.genericType, v);
            }

            try (ByteBufInputStream is = new ByteBufInputStream(byteBuf)) {
                return ByteString.readFrom(is);
            }
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException(e);
        } finally {
            byteBuf.release();
        }

    }

    @SuppressWarnings("unchecked")
    private static ByteString parseArray(ParamDetail paramDetail, Object obj) {
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();

        try (ByteBufOutputStream os = new ByteBufOutputStream(byteBuf)) {
            int len = Array.getLength(obj);
            checkSize(len);

            for (int i = 0; i < len; ++i) {
                Object elem = Array.get(obj, i);
                if (elem == null) {
                    throw new CodecException("list元素不能为空");
                }

                writeValue(os, paramDetail.genericType, obj);
            }

            try (ByteBufInputStream is = new ByteBufInputStream(byteBuf)) {
                return ByteString.readFrom(is);
            }
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException(e);
        } finally {
            byteBuf.release();
        }
    }

    private static ByteString parseValue(ParamDetail paramDetail, Object obj) {
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();

        try (ByteBufOutputStream os = new ByteBufOutputStream(byteBuf)) {
            writeValue(os, paramDetail.type, obj);

            try (ByteBufInputStream is = new ByteBufInputStream(byteBuf)) {
                return ByteString.readFrom(is);
            }
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException(e);
        } finally {
            byteBuf.release();
        }

    }

    private static void writeValue(DataOutput dataOutput, ParamType paramType, Object obj) throws Exception {
        switch (paramType) {
            case BOOLEAN:
                dataOutput.writeBoolean((Boolean) obj);
                break;
            case BYTE:
                dataOutput.writeByte((Byte) obj);
                break;
            case SHORT:
                dataOutput.writeShort((Short) obj);
                break;
            case INT:
                dataOutput.writeInt((Integer) obj);
                break;
            case LONG:
                dataOutput.writeLong((Long) obj);
                break;
            case FLOAT:
                dataOutput.writeFloat((Float) obj);
                break;
            case DOUBLE:
                dataOutput.writeDouble((Double) obj);
                break;
            case STRING:
                dataOutput.writeUTF((String) obj);
                break;
            case NET_DATA:
                ((NetData) obj).write(dataOutput);
                break;
            default:
                throw new CoreException("未知类型");
        }
    }

    @Override
    public String name() {
        return methodDetail.name();
    }

    @Override
    public MethodDetail methodDetail() {
        return methodDetail;
    }

    @Override
    public Future<Base.Body> handle(Base.Auth auth, Base.Body params) {
        throw new UnsupportedOperationException("handle");
    }

}
