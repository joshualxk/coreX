package corex.core.impl;

/**
 * Created by Joshua on 2018/2/26.
 */
public class CoreXThread extends Thread {

    private final boolean worker;
    private final long maxExecTime;
    private long execStart;
    private AbstractContext context;

    public CoreXThread(Runnable target, String name, boolean worker, long maxExecTime) {
        super(target, name);
        this.worker = worker;
        this.maxExecTime = maxExecTime;
    }

    AbstractContext getContext() {
        return context;
    }

    void setContext(AbstractContext context) {
        this.context = context;
    }

    public final void executeStart() {
        execStart = System.nanoTime();
    }

    public final void executeEnd() {
        execStart = 0;
    }

    public long startTime() {
        return execStart;
    }

    public boolean isWorker() {
        return worker;
    }

    public long getMaxExecTime() {
        return maxExecTime;
    }
}
