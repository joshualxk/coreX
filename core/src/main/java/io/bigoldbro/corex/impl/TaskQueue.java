package io.bigoldbro.corex.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.Executor;

/**
 * Created by Joshua on 2018/3/8.
 */
public class TaskQueue {

    private static final Logger log = LoggerFactory.getLogger(TaskQueue.class);

    private final LinkedList<Runnable> tasks = new LinkedList<>();

    private boolean running;

    private final Runnable runner;

    public TaskQueue() {
        runner = () -> {
            for (; ; ) {
                final Runnable task;
                synchronized (tasks) {
                    task = tasks.poll();
                    if (task == null) {
                        running = false;
                        return;
                    }
                }
                try {
                    task.run();
                } catch (Throwable t) {
                    log.error("Caught unexpected Throwable", t);
                }
            }
        };
    }

    public void execute(Runnable task, Executor executor) {
        synchronized (tasks) {
            tasks.add(task);
            if (!running) {
                running = true;
                executor.execute(runner);
            }
        }
    }
}
