package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.exception.BizException;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.impl.CoreXImpl;
import io.bigoldbro.corex.impl.FailedFuture;
import io.bigoldbro.corex.impl.FutureImpl;
import io.bigoldbro.corex.impl.handler.InitialHandler;
import io.bigoldbro.corex.proto.Base;
import io.bigoldbro.corex.utils.CoreXUtil;
import io.bigoldbro.corex.utils.PackageUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Joshua on 2018/3/22.
 */
public class StandaloneClient {

    private final EventLoopGroup worker;
    private final Map<String, ModuleInfo> modules = new HashMap<>();
    private final long timeoutMillis;
    private final int serverId;
    private final int role = ConstDefine.ROLE_CLIENT;
    private final long startTime = System.currentTimeMillis();

    private static class PrefixThreadFactory implements ThreadFactory {

        private String prefix;
        private AtomicInteger count = new AtomicInteger();

        public PrefixThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + "_" + count.getAndIncrement());
            return t;
        }
    }

    public StandaloneClient(String packageName, int serverId, long timeoutMillis) throws Exception {
        worker = new NioEventLoopGroup(1, new PrefixThreadFactory("StandaloneClient-eventLoop"));
        this.serverId = serverId;
        if (timeoutMillis < 1) {
            throw new IllegalArgumentException("Cannot set timeout < 1 ms");
        }
        this.timeoutMillis = timeoutMillis;
        init(packageName);
    }

    private void init(String packageName) throws Exception {

        for (Class<?> clz : PackageUtil.getClassesForPackage(packageName)) {
            Module module = clz.getAnnotation(Module.class);
            if (module == null) {
                continue;
            }

            ModuleInfo moduleInfo = new ClientModuleScanner(clz).parse();

            if (moduleInfo.size() > 0) {
                if (modules.putIfAbsent(module.address(), moduleInfo) != null) {
                    throw new CoreException("Module名重复:" + module.address());
                }
            }

        }
    }

    @SuppressWarnings("unchecked")
    public <T> T connect(String host, int port, Base.Auth auth, Class<T> clz) {
        Module module = clz.getAnnotation(Module.class);
        if (module == null) {
            throw new CoreException("不支持的类型:" + clz.getName());
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ModuleInfo moduleInfo = modules.get(module.address());
        if (moduleInfo == null) {
            throw new CoreException("不存在的模块:" + module.address());
        }
        return (T) Proxy.newProxyInstance(cl, new Class<?>[]{clz}, new ApiInvocationHandler(host, port, auth, moduleInfo));
    }

    private class ApiInvocationHandler implements InvocationHandler {
        private final String host;
        private final int port;
        private final Base.Auth auth;
        private final ModuleInfo moduleInfo;

        public ApiInvocationHandler(String host, int port, Base.Auth auth, ModuleInfo moduleInfo) {
            this.host = host;
            this.port = port;
            this.auth = auth;
            this.moduleInfo = moduleInfo;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Api api = method.getAnnotation(Api.class);
            if (api == null) {
                throw new CoreException("不支持的方法:" + method.getName());
            }

            RpcHandler rpcHandler = moduleInfo.getHandler(api.value());
            if (rpcHandler == null) {
                throw new CoreException("找不到方法:" + method.getName());
            }

            final boolean isVoid = rpcHandler.methodDetail().returnDetail.isVoid;
            final boolean isAsync = rpcHandler.methodDetail().returnDetail.isAsync;

            CoreXImpl.ensureBlockSafe();

            Base.Body body = rpcHandler.convert(args);

            Base.Method m = CoreXUtil.newMethod(moduleInfo.module().address(), api.value(), moduleInfo.module().version());
            Base.Request request = CoreXUtil.newRequest(1, auth, m, body);

            Base.Payload payload = Base.Payload.newBuilder()
                    .setId(isVoid ? 0 : 1)
                    .setRequest(request)
                    .build();

            Future<Object> future = new FutureImpl<>();

            io.netty.bootstrap.Bootstrap b = new io.netty.bootstrap.Bootstrap();
            b.group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            CoreXUtil.initPipeline(p);

                            InitialHandler initialHandler = new InitialHandler(serverId, role, startTime);
                            p.addLast("initialHandler", initialHandler);
                            initialHandler.setServerAuthHandler(ping -> {
                                p.channel().writeAndFlush(payload);
                                if (isVoid) {
                                    future.tryComplete();
                                }
                            });
                            if (!isVoid) {
                                initialHandler.setPayloadHandler(pl -> {
                                    if (pl.hasResponse()) {
                                        Base.Response response = pl.getResponse();
                                        if (CoreXUtil.isSuccessResponse(response)) {

                                            if (response.getBody().getFieldsCount() < 1) {
                                                future.tryFail(ExceptionDefine.SYSTEM_ERR.build());
                                                return;
                                            }
                                            Object o = ServerRpcHandler.unwrapObj(rpcHandler.methodDetail().returnDetail, response.getBody().getFields(0));

                                            future.tryComplete(o);
                                        } else {
                                            future.tryFail(BizException.newException(response.getCode(), response.getMsg()));
                                        }
                                    } else {
                                        future.tryFail(ExceptionDefine.SYSTEM_ERR.build());
                                    }

                                });
                            }
                        }
                    });

            b.connect(host, port).addListener(fut -> {
                if (fut.isSuccess()) {
                    Channel channel = ((ChannelFuture) fut).channel();
                    future.addHandler(h -> {
                        channel.close();
                    });
                    channel.closeFuture().addListener(v -> future.tryFail(ExceptionDefine.CONN_FAIL.build()));
                } else {
                    future.fail(fut.cause());
                }
            });

            try {
                if (!isAsync) {
                    return future.sync().result();
                }

                return future;
            } catch (Throwable t) {
                throw t;
            }
        }
    }
}
