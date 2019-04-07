package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.impl.handler.InitialHandler;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.model.Payload;
import io.bigoldbro.corex.model.RpcRequest;
import io.bigoldbro.corex.model.RpcResponse;
import io.bigoldbro.corex.rpc.ModuleParams;
import io.bigoldbro.corex.rpc.ModuleScanner;
import io.bigoldbro.corex.rpc.RpcHandler;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Joshua on 2018/3/22.
 */
public class StandaloneClient {

    private final EventLoopGroup worker;
    private final Map<String, ModuleParams> modules = new HashMap<>();
    private final long timeoutMillis;
    private final int serverId;
    private final int role = ConstDefine.ROLE_ADMIN;
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

        ModuleScanner moduleScanner = ModuleScanner.clientModuleScanner();
        for (Class<?> clz : PackageUtil.getClassesForPackage(packageName)) {
            Module module = clz.getAnnotation(Module.class);
            if (module == null) {
                continue;
            }

            ModuleParams moduleParams = moduleScanner.parse(clz);

            if (moduleParams.size() > 0) {
                if (modules.putIfAbsent(module.address(), moduleParams) != null) {
                    throw new CoreException("Module名重复:" + module.address());
                }
            }

        }
    }

    @SuppressWarnings("unchecked")
    public <T> T connect(String host, int port, Class<T> clz) {
        Module module = clz.getAnnotation(Module.class);
        if (module == null) {
            throw new CoreException("不支持的类型:" + clz.getName());
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ModuleParams moduleParams = modules.get(module.address());
        if (moduleParams == null) {
            throw new CoreException("不存在的模块:" + module.address());
        }
        return (T) Proxy.newProxyInstance(cl, new Class<?>[]{clz}, new ApiInvocationHandler(host, port, moduleParams));
    }

    private class ApiInvocationHandler implements InvocationHandler {
        private final String host;
        private final int port;
        private final ModuleParams moduleParams;

        public ApiInvocationHandler(String host, int port, ModuleParams moduleParams) {
            this.host = host;
            this.port = port;
            this.moduleParams = moduleParams;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return invoke0(proxy, method, args);
        }

        private JoHolder invoke0(Object proxy, Method method, Object[] args) throws Throwable {
            Api api = method.getAnnotation(Api.class);
            if (api == null) {
                throw new CoreException("不支持的方法:" + method.getName());
            }

            if (api.type() != ConstDefine.AUTH_TYPE_ADMIN) {
                throw new CoreException("非admin调用api");
            }

            RpcHandler rpcHandler = moduleParams.getHandler(api.value());
            if (rpcHandler == null) {
                throw new CoreException("找不到方法:" + method.getName());
            }

            boolean isVoidType = rpcHandler.isVoidType();

            JsonObjectImpl argsJo = rpcHandler.convert(args);

            RpcRequest rpcRequest = RpcRequest.newAdminRpcRequest(1, moduleParams.module().address(), api.value(), moduleParams.module().version(), argsJo);
            Payload payload = Payload.newPayload(isVoidType ? 0 : 1, rpcRequest);

            CompletableFuture<JoHolder> completableFuture = new CompletableFuture<>();
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
                                if (isVoidType) {
                                    completableFuture.complete(null);
                                }
                            });
                            initialHandler.setPayloadHandler(pl -> {
                                if (pl.hasRpcResponse()) {
                                    RpcResponse rpcResponse = pl.getRpcResponse();
                                    if (CoreXUtil.isSuccessResponse(rpcResponse)) {
                                        JsonObjectImpl jo = rpcResponse.getBody();
                                        completableFuture.complete(JoHolder.newSync(jo));
                                    } else {
                                        completableFuture.completeExceptionally(ExceptionDefine.newException(rpcResponse.getCode(), rpcResponse.getMessage()));
                                    }

                                }

                            });
                        }
                    });

            Channel channel = null;
            ChannelFuture future = b.connect(host, port).sync();
            if (future.isSuccess()) {
                channel = future.channel();
            } else {
                completableFuture.completeExceptionally(future.cause());
            }

            try {
                return completableFuture.get(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                throw e.getCause();
            } finally {
                if (channel != null) {
                    channel.close();
                }
            }
        }
    }
}
