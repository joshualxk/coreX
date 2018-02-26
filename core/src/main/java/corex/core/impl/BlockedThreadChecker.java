package corex.core.impl;

import corex.core.MsgHandler;
import corex.core.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

/**
 * Created by Joshua on 2018/2/26.
 */
public class BlockedThreadChecker {

    private static final Logger log = LoggerFactory.getLogger(BlockedThreadChecker.class);

    private static final Object O = new Object();
    private final Map<CoreXThread, Object> threads = new WeakHashMap<>();
    private final Timer timer;

    BlockedThreadChecker(MsgHandler msgHandler, long interval, long warningExceptionTime) {
        timer = new Timer("corex-blocked-checker", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (BlockedThreadChecker.this) {
                    long now = System.nanoTime();
                    for (CoreXThread thread : threads.keySet()) {
                        long execStart = thread.startTime();
                        long dur = now - execStart;
                        final long timeLimit = thread.getMaxExecTime();
                        if (execStart != 0 && dur > timeLimit) {
                            final String message = "Thread " + thread + " has been blocked for " + (dur / 1000000) + " ms, time limit is " + (timeLimit / 1000000);
                            if (dur <= warningExceptionTime) {
                                log.warn(message);
                            } else {
                                Exception stackTrace = new CoreException("Thread blocked");
                                stackTrace.setStackTrace(thread.getStackTrace());
                                log.warn(message, stackTrace);
                            }
                        }
                    }
                }
                msgHandler.removeExpireMsg();
            }
        }, interval, interval);
    }

    public synchronized void registerThread(CoreXThread thread) {
        threads.put(thread, O);
    }

    public void close() {
        timer.cancel();
    }
}
