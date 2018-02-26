package corex.core.impl;

import corex.core.CoreX;
import corex.core.Handler;
import corex.core.Service;
import corex.core.exception.CoreException;

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
