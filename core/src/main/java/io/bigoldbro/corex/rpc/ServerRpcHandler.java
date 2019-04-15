package io.bigoldbro.corex.rpc;

import com.google.protobuf.ByteString;
import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.NetData;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.exception.BizException;
import io.bigoldbro.corex.exception.CodecException;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.impl.FutureImpl;
import io.bigoldbro.corex.impl.SucceededFuture;
import io.bigoldbro.corex.proto.Base;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import java.io.DataInput;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Joshua on 2018/3/8.
 */
public class ServerRpcHandler implements RpcHandler {

    private final MethodDetail methodDetail;
    private final Object invoker;
    private final int requireType;

    public ServerRpcHandler(MethodDetail methodDetail, Object invoker, int requireType) {
        this.methodDetail = methodDetail;
        this.invoker = invoker;
        this.requireType = requireType;
    }

    public Future<Base.Body> handle(Base.Auth auth, Base.Body params) {

        // 授权类型不一致
        final int clientType = auth.getType();
        if (requireType != clientType) {

            if (clientType == ConstDefine.AUTH_TYPE_CLIENT) {
                if (requireType == ConstDefine.AUTH_TYPE_NON) {
                    // 已登录可以访问不需登录接口
                } else {
                    // 对玩家不可见
                    throw ExceptionDefine.NOT_FOUND.build();
                }
            } else if (clientType == ConstDefine.AUTH_TYPE_NON) {
                if (requireType == ConstDefine.AUTH_TYPE_CLIENT) {
                    throw ExceptionDefine.NOT_LOGIN.build();
                } else {
                    // 对玩家不可见
                    throw ExceptionDefine.NOT_FOUND.build();
                }
            } else {
                throw ExceptionDefine.NOT_AUTHORIZED.build();
            }
        }

        return invoke(params, methodDetail);
    }

    @Override
    public Base.Body convert(Object[] args) {
        throw new UnsupportedOperationException("convert");
    }

    private Future<Base.Body> invoke(Base.Body params, MethodDetail methodDetail) {
        Object[] objects;
        if (methodDetail.params.isEmpty()) {
            objects = null;
        } else {
            objects = new Object[methodDetail.params.size()];

            if (params.getFieldsCount() != methodDetail.params.size()) {
                throw new CodecException("参数数量不一致");
            }

            List<ByteString> fieldsList = params.getFieldsList();

            int i = 0;
            for (ByteString byteString : fieldsList) {
                ParamDetail paramDetail = methodDetail.params.get(i);

                objects[i++] = unwrapObj(paramDetail, byteString);
            }

        }

        try {
            Object ret = methodDetail.method.invoke(invoker, objects);

            if (ret == null) {
                if (!methodDetail.returnDetail.isVoid) {
                    throw new CoreException("response is null");
                }
                return null;
            } else {
                if (methodDetail.returnDetail.isAsync) {
                    Future<Object> future = (Future<Object>) ret;

                    Future<Base.Body> future1 = new FutureImpl<>();
                    future.addHandler(ar -> {
                        if (ar.succeeded()) {
                            ByteString wrapObj = ClientRpcHandler.wrapObj(methodDetail.returnDetail, ar.result());
                            Base.Body body = Base.Body.newBuilder()
                                    .addFields(wrapObj)
                                    .build();
                            future1.complete(body);
                        } else {
                            future1.fail(ar.cause());
                        }
                    });

                    return future1;
                } else {
                    ByteString wrapObj = ClientRpcHandler.wrapObj(methodDetail.returnDetail, ret);
                    Base.Body body = Base.Body.newBuilder()
                            .addFields(wrapObj)
                            .build();
                    return new SucceededFuture<>(body);
                }

            }

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof BizException) {
                throw (BizException) cause;
            } else if (cause instanceof CoreException) {
                throw (CoreException) cause;
            } else {
                throw new CoreException(cause);
            }
        } catch (IllegalAccessException e) {
            throw new CoreException(e);
        }
    }

    static Object unwrapObj(ParamDetail paramDetail, ByteString byteString) {
        Object val;
        switch (paramDetail.type) {
            case UNSUPPORTED:
                throw new CoreException("不支持的类型");
            case LIST:
                val = parseList(paramDetail, byteString);
                break;
            case MAP:
                val = parseMap(paramDetail, byteString);
                break;
            case ARRAY:
                val = parseArray(paramDetail, byteString);
                break;
            default:
                val = parseValue(paramDetail, byteString);
                break;
        }
        return val;
    }

    private static List parseList(ParamDetail pd, ByteString byteString) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteString.toByteArray());

        try (ByteBufInputStream is = new ByteBufInputStream(byteBuf)) {
            // list size
            int len = is.readShort();
            ClientRpcHandler.checkSize(len);

            List<Object> list = new ArrayList<>(len);

            for (int i = 0; i < len; ++i) {
                list.add(readValue(is, pd.genericType, pd.extClz));
            }

            return list;

        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException(e);
        } finally {
            byteBuf.release();
        }
    }

    private static Map parseMap(ParamDetail pd, ByteString byteString) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteString.toByteArray());

        try (ByteBufInputStream is = new ByteBufInputStream(byteBuf)) {
            // list size
            int len = is.readShort();
            ClientRpcHandler.checkSize(len);

            Map<String, Object> map = new HashMap<>(len);

            for (int i = 0; i < len; ++i) {
                String k = is.readUTF();
                map.put(k, readValue(is, pd.genericType, pd.extClz));
            }

            return map;

        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException(e);
        } finally {
            byteBuf.release();
        }
    }

    private static Object parseArray(ParamDetail pd, ByteString byteString) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteString.toByteArray());

        try (ByteBufInputStream is = new ByteBufInputStream(byteBuf)) {
            // array size
            int len = is.readShort();
            ClientRpcHandler.checkSize(len);

            Object val = Array.newInstance(pd.extClz, len);

            for (int i = 0; i < len; ++i) {
                Array.set(val, i, readValue(is, pd.genericType, pd.extClz));
            }

            return val;

        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException(e);
        } finally {
            byteBuf.release();
        }
    }

    private static Object parseValue(ParamDetail pd, ByteString byteString) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteString.toByteArray());

        try (ByteBufInputStream is = new ByteBufInputStream(byteBuf)) {
            return readValue(is, pd.type, pd.extClz);
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CoreException("参数类型不合法:" + pd.type);
        } finally {
            byteBuf.release();
        }

    }

    private static Object readValue(DataInput dataInput, ParamType paramType, Class<?> extClz) throws Exception {
        switch (paramType) {
            case BOOLEAN:
                return dataInput.readBoolean();
            case BYTE:
                return dataInput.readByte();
            case SHORT:
                return dataInput.readShort();
            case INT:
                return dataInput.readInt();
            case LONG:
                return dataInput.readLong();
            case FLOAT:
                return dataInput.readFloat();
            case DOUBLE:
                return dataInput.readDouble();
            case STRING:
                return dataInput.readUTF();
            case NET_DATA:
                NetData netData = ((Class<? extends NetData>) extClz).newInstance();
                netData.read(dataInput);
                return netData;
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
}
