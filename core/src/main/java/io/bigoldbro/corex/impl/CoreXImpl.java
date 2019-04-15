package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.*;
import io.bigoldbro.corex.annotation.BlockControl;
import io.bigoldbro.corex.annotation.Value;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.module.BroadcastModule;
import io.bigoldbro.corex.proto.Base;
import io.bigoldbro.corex.rpc.RpcClient;
import io.bigoldbro.corex.utils.CoreXUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static io.bigoldbro.corex.utils.CoreXUtil.isRole;

/**
 * Created by Joshua on 2018/2/26.
 */
public class CoreXImpl implements CoreX {

    private static final Logger log = LoggerFactory.getLogger(CoreXImpl.class);

    public static final int DEFAULT_ACCEPTOR_POOL_SIZE = 1;

    public static final int DEFAULT_EVENT_LOOP_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    public static final int DEFAULT_WORKER_POOL_SIZE = 8;

    public static final int DEFAULT_MAX_PENDING_MSG_NUM = 10000;

    public static final long DEFAULT_REQUEST_TIME_OUT = 60_000; // 60s

    public static final long DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME = 2L * 1000 * 1000000; // 2s

    public static final long DEFAULT_MAX_WORKER_EXECUTE_TIME = 60L * 1000 * 1000000; // 60s

    public static final long DEFAULT_BLOCKED_THREAD_CHECK_INTERVAL = 1000;

    public static final long DEFAULT_WARNING_EXCEPTION_TIME = 5L * 1000 * 1000000;

    private final Map<String, Service> serviceMap = new ConcurrentHashMap<>();
    private final DefaultMsgHandler msgHandler;
    private final List<Handler<Base.Broadcast>> internalBroadcastHandlers = new Vector<>();
    private final List<Handler<Base.Broadcast>> externalBroadcastHandlers = new Vector<>();
    private final ConcurrentMap<Long, InternalTimerHandler> timeouts = new ConcurrentHashMap<>();
    private final RpcClient rpcClient;
    private final AtomicLong timeoutCounter = new AtomicLong(0);
    private final AtomicLong msgCounter = new AtomicLong(1);
    private final EventLoopGroup acceptorEventLoopGroup;
    private final EventLoopGroup eventLoopGroup;
    private final ExecutorService workerPool;
    private final CoreXConfig coreXConfig;
    private final long startTime;

    @Value("corex.id")
    private int serverId;
    @Value("corex.role")
    private int role;

    public CoreXImpl(CoreXConfig coreXConfig) {

        try {
            rpcClient = new RpcClient(this, ConstDefine.DEFAULT_MODULE_PACKAGE);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CoreException("初始化rpcClient失败");
        }

        this.coreXConfig = Objects.requireNonNull(coreXConfig, "coreXConfig");
        this.coreXConfig.init(this);

        if (this.role == ConstDefine.ROLE_LOCAL) {
            throw new CoreException("role配置错误");
        }
        this.startTime = CoreXUtil.sysTime();

        msgHandler = new DefaultMsgHandler(DEFAULT_MAX_PENDING_MSG_NUM, DEFAULT_REQUEST_TIME_OUT);

        BlockedThreadChecker checker = new BlockedThreadChecker(this, DEFAULT_BLOCKED_THREAD_CHECK_INTERVAL, DEFAULT_WARNING_EXCEPTION_TIME);
        acceptorEventLoopGroup = new NioEventLoopGroup(DEFAULT_ACCEPTOR_POOL_SIZE,
                new CoreXThreadFactory("corex-acceptor-", checker, false, DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME));
        eventLoopGroup = new NioEventLoopGroup(DEFAULT_EVENT_LOOP_POOL_SIZE,
                new CoreXThreadFactory("corex-eventLoop-", checker, false, DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME));
        workerPool = Executors.newFixedThreadPool(DEFAULT_WORKER_POOL_SIZE,
                new CoreXThreadFactory("corex-worker-", checker, true, DEFAULT_MAX_WORKER_EXECUTE_TIME));
    }

    @Override
    public int serverId() {
        return serverId;
    }

    @Override
    public long startTime() {
        return startTime;
    }

    @Override
    public int role() {
        return role;
    }

    @Override
    public CoreXConfig config() {
        return coreXConfig;
    }

    @Override
    public EventLoopGroup acceptorEventLoopGroup() {
        return acceptorEventLoopGroup;
    }

    @Override
    public EventLoopGroup eventLoopGroup() {
        return eventLoopGroup;
    }

    @Override
    public ExecutorService executorService() {
        return workerPool;
    }

    public static Context getContext() {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof CoreXThread) {
            return ((CoreXThread) currentThread).getContext();
        }
        return null;
    }

    public static Context ensureContext() {
        Context context = getContext();
        if (context == null) {
            throw new CoreException("context为空");
        }
        return context;
    }

    public static Context ensureContext(Context ctx) {
        Context context = getContext();
        if (context != ctx) {
            throw new CoreException("context错误");
        }
        return context;
    }

    public static void ensureNoContext() {
        if (getContext() != null) {
            throw new CoreException("context不为空");
        }
    }

    // 是否能执行阻塞任务
    public static void ensureBlockSafe() {
        Context ctx = getContext();
        if (ctx != null && !ctx.isWorker()) {
            throw new CoreException("io线程不能阻塞");
        }
    }

    private Context createContext(String name, BlockControl bc, Service service) {
        switch (bc) {
            case NON_BLOCK:
                return new EventLoopContext(this, name, service);
            case BLOCK:
                return new WorkerContext(this, name, service);
            case MULTI_THREADED:
                return new MultiThreadedWorkerContext(this, name, service);
        }
        throw new CoreException("不支持的阻塞类型");
    }

    @Override
    public void startService(Class<? extends Service> serviceClz, Handler<AsyncResult<String>> resultHandler) throws Exception {
        Service service = serviceClz.newInstance();
        String name = service.name();
        BlockControl bc = service.bc();

        if (serviceMap.containsKey(name)) {
            resultHandler.handle(Future.failedFuture(new CoreException("服务 " + name + " 已启动!")));
            return;
        }

        log.info("启动服务, name:{}, bc:{}.", name, bc.toString());

        Context context = createContext(name, bc, service);

        context.runOnContext(v -> {
            Future<Void> future = Future.future();
            future.addHandler(ar -> {
                if (ar.succeeded()) {
                    if (serviceMap.putIfAbsent(name, service) != null) {
                        resultHandler.handle(Future.failedFuture(new CoreException("服务 " + name + " 已启动!")));
                        return;
                    }
                }
                resultHandler.handle(new SucceededFuture<>(name));
            });

            try {
                coreXConfig.init(service);
                service.init(context);
                service.start(future);
            } catch (Throwable t) {
                future.fail(t);
            }
        });
    }

    @Override
    public void stopService(String name, Handler<AsyncResult<Void>> resultHandler) {
        ensureNoContext();
        if (!serviceMap.containsKey(name)) {
            log.warn("服务 " + name + " 已关闭!");
            return;
        }

        Service service = serviceMap.get(name);
        service.context().runOnContext(v -> {
            Future<Void> future = Future.future();
            future.addHandler(ar -> {
                if (ar.succeeded()) {
                    serviceMap.remove(name);
                }
                resultHandler.handle(ar);
            });

            try {
                service.stop(future);
            } catch (Throwable t) {
                future.fail(t);
            }
        });
    }

    private Msg wrapMsg(Context context, boolean needReply, Object raw) {
        long id = needReply ? msgCounter.getAndIncrement() : 0;
        Base.Payload payload;
        if (raw instanceof Base.Request) {
            Base.Request request = (Base.Request) raw;
            payload = Base.Payload.newBuilder()
                    .setId(id)
                    .setRequest(request)
                    .addRoutes(context.name())
                    .build();
        } else if (raw instanceof Base.Broadcast) {
            Base.Broadcast broadcast = (Base.Broadcast) raw;
            payload = Base.Payload.newBuilder()
                    .setId(id)
                    .setBroadcast(broadcast)
                    .addRoutes(context.name())
                    .build();
        } else if (raw instanceof Base.Payload) {
            payload = (Base.Payload) raw;
        } else {
            throw new CoreException("不支持的消息类型:" + (raw == null ? null : raw.getClass().getName()));
        }

        return new InternalMsg(context, id, payload);
    }

    @Override
    public void sendMessage(String address, Object msg, Handler<AsyncResult<Base.Payload>> replyHandler) {
        Context context = ensureContext();
        Service service = getService(address);

        Objects.requireNonNull(msg);
        if (service == null) {
            if (replyHandler != null) {
                context.runOnContext(v -> replyHandler.handle(Future.failedFuture(ExceptionDefine.NOT_FOUND.build())));
            } else {
                throw ExceptionDefine.NOT_FOUND.build();
            }
        } else {
            Msg sendMsg = wrapMsg(context, replyHandler != null, msg);
            if (replyHandler != null) {
                try {
                    onMsgSent(sendMsg.id(), replyHandler);
                } catch (Exception e) {
                    replyHandler.handle(Future.failedFuture(e));
                    return;
                }
            }
            service.context().runOnContext(v -> {
                service.handleMsg(sendMsg);
            });
        }
    }

    @Override
    public void broadcastMessage(Base.Broadcast broadcast) {
        sendMessage(ServiceNameDefine.BROADCAST, broadcast, null);
    }

    @Override
    public void createNetServer(int port, ChannelHandler channelHandler, Handler<AsyncResult<Void>> resultHandler) {
        Context context = ensureContext();

        ServerBootstrap b = new ServerBootstrap();
        b.group(acceptorEventLoopGroup(), context.isWorker() ? eventLoopGroup : context.eventLoop())
                .channel(NioServerSocketChannel.class)
                .childHandler(channelHandler);

        b.bind(port).addListener(future -> {
            if (resultHandler != null) {
                context.runOnContext(v -> {
                    if (future.isSuccess()) {
                        resultHandler.handle(Future.succeededFuture());
                    } else {
                        resultHandler.handle(Future.failedFuture(future.cause()));
                    }
                });
            }
        });
    }

    @Override
    public void connectNetServer(String host, int port, ChannelHandler channelHandler, Handler<AsyncResult<Channel>> resultHandler) {
        Context context = ensureContext();

        Bootstrap b = new Bootstrap();
        b.group(context.isWorker() ? eventLoopGroup : context.eventLoop())
                .channel(NioSocketChannel.class)
                .handler(channelHandler);

        b.connect(host, port).addListener(future -> {
            if (resultHandler != null) {
                context.runOnContext(v -> {
                    if (future.isSuccess()) {
                        resultHandler.handle(Future.succeededFuture(((ChannelFuture) future).channel()));
                    } else {
                        resultHandler.handle(Future.failedFuture(future.cause()));
                    }
                });
            }
        });
    }

    @Override
    public long setTimer(long delay, Handler<Long> handler) {
        return scheduleTimeout(ensureContext(), handler, delay, false);
    }

    @Override
    public long setPeriodic(long delay, Handler<Long> handler) {
        return scheduleTimeout(ensureContext(), handler, delay, true);
    }

    @Override
    public boolean cancelTimer(long id) {
        InternalTimerHandler handler = timeouts.remove(id);
        if (handler != null) {
            return handler.cancel();
        } else {
            return false;
        }
    }

    @Override
    public <T> T asyncAgent(Class<T> clz) {
        T t = rpcClient.apiAgent(clz);
        if (t == null) {
            throw new CoreException("找不到对应服务:" + clz.getName());
        }
        return t;
    }

    @Override
    public BroadcastModule broadcast() {
        return rpcClient.broadcastAgent();
    }

    @Override
    public int subscribeBroadcast(Handler<Base.Broadcast> internal, Handler<Base.Broadcast> external) {
        Context context = ensureContext();

        int added = 0;
        if (internal != null && internalBroadcastHandlers.add(e -> context.runOnContext(v -> internal.handle(e)))) {
            added++;
        }
        if (external != null && externalBroadcastHandlers.add(e -> context.runOnContext(v -> external.handle(e)))) {
            added++;
        }
        return added;
    }

    @Override
    public void onBroadcast(Base.Broadcast broadcast) {
        List<Handler<Base.Broadcast>> handlers = broadcast.getInternal() ? internalBroadcastHandlers : externalBroadcastHandlers;
        for (Handler<Base.Broadcast> h : handlers) {
            h.handle(broadcast);
        }
    }

    private Service getService(String name) {
        Service s;
        if (ServiceNameDefine.BROADCAST.equals(name)) {
            if (isRole(role(), ConstDefine.ROLE_GATEWAY)) {
                s = serviceMap.get(ServiceNameDefine.HARBOR_SERVER);
            } else {
                s = serviceMap.get(ServiceNameDefine.HARBOR_CLIENT);
            }
        } else {
            s = serviceMap.get(name);
            if (s == null) {
                if (isRole(role(), ConstDefine.ROLE_GATEWAY)) {
                    s = serviceMap.get(ServiceNameDefine.HARBOR_CLIENT);
                }
            }
        }
        return s;
    }

    private long scheduleTimeout(Context context, Handler<Long> handler, long delay, boolean periodic) {
        if (delay < 1) {
            throw new IllegalArgumentException("Cannot schedule a timer with delay < 1 ms");
        }
        long timerId = timeoutCounter.getAndIncrement();
        InternalTimerHandler task = new InternalTimerHandler(timerId, handler, periodic, delay, context);
        timeouts.put(timerId, task);
        return timerId;
    }

    @Override
    public void onMsgSent(long id, Handler<AsyncResult<Base.Payload>> handler) {
        Context context = ensureContext();
        msgHandler.onMsgSent(id, ar -> {
            context.runOnContext(v -> handler.handle(ar));
        });
    }

    @Override
    public void onMsgReply(long id, AsyncResult<Base.Payload> resp) {
        msgHandler.onMsgReply(id, resp);
    }

    @Override
    public void removeExpireMsg() {
        msgHandler.removeExpireMsg();
    }

    public Map<String, String> info() {
        Map<String, String> map = new HashMap<>();
        map.put("serverId", String.valueOf(serverId));
        map.put("role", String.valueOf(role));
        map.put("startTime", String.valueOf(startTime));

        Set<Map.Entry<String, Service>> entries = serviceMap.entrySet();
        List<String> services = new ArrayList<>(entries.size());
        for (Map.Entry<String, Service> entry : entries) {
            services.add(entry.getKey());
        }
        map.put("services", services.toString());
        map.put("timeoutsNum", String.valueOf(timeouts.size()));
        map.put("pendingMsgNum", String.valueOf(msgHandler.pendingMsgNum()));

        return map;
    }

    private class InternalTimerHandler implements Handler<Void> {

        final long timerID;
        final Handler<Long> handler;
        final boolean periodic;
        final long delay;
        final Context context;
        final java.util.concurrent.Future<?> future;
        final AtomicBoolean cancelled;

        InternalTimerHandler(long timerID, Handler<Long> handler, boolean periodic, long delay, Context context) {
            this.timerID = timerID;
            this.handler = handler;
            this.periodic = periodic;
            this.delay = delay;
            this.context = context;
            this.cancelled = new AtomicBoolean();
            EventLoop el = context.eventLoop();
            Runnable toRun = () -> context.runOnContext(this);
            if (periodic) {
                future = el.scheduleAtFixedRate(toRun, delay, delay, TimeUnit.MILLISECONDS);
            } else {
                future = el.schedule(toRun, delay, TimeUnit.MILLISECONDS);
            }
        }

        boolean cancel() {
            if (cancelled.compareAndSet(false, true)) {
                future.cancel(false);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void handle(Void v) {
            if (!cancelled.get()) {
                try {
                    handler.handle(timerID);
                } finally {
                    if (!periodic) {
                        cleanupNonPeriodic();
                    }
                }
            }
        }

        private void cleanupNonPeriodic() {
            CoreXImpl.this.timeouts.remove(timerID);
        }

    }
}
