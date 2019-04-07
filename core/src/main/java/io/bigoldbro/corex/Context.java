package io.bigoldbro.corex;

import io.netty.channel.EventLoop;

/**
 * Created by Joshua on 2018/2/26.
 */
public interface Context {

    CoreX coreX();

    String name();

    EventLoop eventLoop();

    boolean isWorker();

    boolean isMultiThreaded();

    void runOnContext(Handler<Void> action);

    void executeFromIO(Handler<Void> action);

    <V> void executeBlocking(Handler<Future<V>> blockingHandler, boolean ordered, Handler<AsyncResult<V>> resultHandler);

}
