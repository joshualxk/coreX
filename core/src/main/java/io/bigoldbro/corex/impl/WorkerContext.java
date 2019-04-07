package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.CoreX;
import io.bigoldbro.corex.Handler;
import io.bigoldbro.corex.Service;

/**
 * Created by Joshua on 2018/2/26.
 */
public class WorkerContext extends AbstractContext {

    public WorkerContext(CoreX coreX, String name, Service service) {
        super(coreX, name, service);
    }

    @Override
    protected void runAsync(Handler<Void> action) {
        taskQueue.execute(wrappedTask(action), executorService);
    }

    @Override
    public boolean isWorker() {
        return true;
    }

    @Override
    public boolean isMultiThreaded() {
        return false;
    }

    @Override
    public void executeFromIO(Handler<Void> action) {
        taskQueue.execute(wrappedTask(action), executorService);
    }
}
