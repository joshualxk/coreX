package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.*;
import io.bigoldbro.corex.exception.CoreException;
import io.netty.channel.EventLoop;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by Joshua on 2018/2/26.
 */
abstract class AbstractContext implements Context {

    protected final CoreX coreX;
    protected final EventLoop eventLoop;
    protected final ExecutorService executorService;
    protected final TaskQueue taskQueue = new TaskQueue();
    protected final String name;
    protected final Service service;

    public AbstractContext(CoreX coreX, String name, Service service) {
        this.coreX = coreX;
        this.eventLoop = coreX.eventLoopGroup().next();
        this.executorService = coreX.executorService();
        this.name = name;
        this.service = service;
    }

    @Override
    public CoreX coreX() {
        return coreX;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public EventLoop eventLoop() {
        return eventLoop;
    }

    @Override
    public void runOnContext(Handler<Void> action) {
        try {
            runAsync(action);
        } catch (RejectedExecutionException ignore) {
            // Pool is already shut down
        }
    }

    protected abstract void runAsync(Handler<Void> action);

    protected Runnable wrappedTask(Handler<Void> action) {
        return () -> {
            Thread currentThread = Thread.currentThread();
            if (currentThread instanceof CoreXThread) {
                CoreXThread coreXThread = (CoreXThread) currentThread;
                AbstractContext oldContext = coreXThread.getContext();
                coreXThread.setContext(AbstractContext.this);

                try {
                    coreXThread.executeStart();
                    action.handle(null);
                } finally {
                    coreXThread.executeEnd();
                    coreXThread.setContext(oldContext);
                }
            } else {
                throw new CoreException("必须在CoreXThread线程运行");
            }
        };
    }

    @Override
    public <V> void executeBlocking(Handler<Future<V>> blockingHandler, boolean ordered, Handler<AsyncResult<V>> resultHandler) {
        Objects.requireNonNull(blockingHandler, "blockingHandler");
        Runnable r = wrappedTask(v -> {
            Future<V> res = Future.future();
            try {
                blockingHandler.handle(res);
            } catch (Throwable e) {
                res.fail(e);
            }

            if (resultHandler != null) {
                runOnContext(v2 -> res.setHandler(resultHandler));
            }
        });

        if (ordered) {
            taskQueue.execute(r, executorService);
        } else {
            executorService.execute(r);
        }
    }
}
