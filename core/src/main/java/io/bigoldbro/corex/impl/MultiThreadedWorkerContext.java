package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.CoreX;
import io.bigoldbro.corex.Handler;
import io.bigoldbro.corex.Service;

/**
 * Created by Joshua on 2018/2/26.
 */
public class MultiThreadedWorkerContext extends WorkerContext {

    public MultiThreadedWorkerContext(CoreX coreX, String name, Service service) {
        super(coreX, name, service);
    }

    @Override
    protected void runAsync(Handler<Void> action) {
        executorService.execute(wrappedTask(action));
    }

    @Override
    public boolean isWorker() {
        return true;
    }

    @Override
    public boolean isMultiThreaded() {
        return true;
    }

    @Override
    public void executeFromIO(Handler<Void> action) {
        taskQueue.execute(wrappedTask(action), executorService);
    }
}
