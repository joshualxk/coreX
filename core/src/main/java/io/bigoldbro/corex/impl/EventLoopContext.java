package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.CoreX;
import io.bigoldbro.corex.Handler;
import io.bigoldbro.corex.Service;
import io.bigoldbro.corex.exception.CoreException;

/**
 * Created by Joshua on 2018/2/26.
 */
public class EventLoopContext extends AbstractContext {

    public EventLoopContext(CoreX coreX, String name, Service service) {
        super(coreX, name, service);
    }

    @Override
    protected void runAsync(Handler<Void> action) {
        eventLoop().execute(wrappedTask(action));
    }

    @Override
    public boolean isWorker() {
        return false;
    }

    @Override
    public boolean isMultiThreaded() {
        return false;
    }

    @Override
    public void executeFromIO(Handler<Void> action) {
        if (eventLoop().inEventLoop()) {
            wrappedTask(action).run();
        } else {
            throw new CoreException("必须在eventLoop线程执行");
        }
    }
}
