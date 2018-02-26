package corex.core.impl;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Joshua on 2018/2/26.
 */
public class CoreXThreadFactory implements ThreadFactory {

    private final String prefix;
    private final AtomicInteger threadCount = new AtomicInteger(0);
    private final BlockedThreadChecker checker;
    private final boolean worker;
    private final long maxExecTime;

    CoreXThreadFactory(String prefix, BlockedThreadChecker checker, boolean worker, long maxExecTime) {
        this.prefix = prefix;
        this.checker = checker;
        this.worker = worker;
        this.maxExecTime = maxExecTime;
    }

    public Thread newThread(Runnable runnable) {
        CoreXThread t = new CoreXThread(runnable, prefix + threadCount.getAndIncrement(), worker, maxExecTime);
        if (checker != null) {
            checker.registerThread(t);
        }
        t.setDaemon(false);
        return t;
    }

}
