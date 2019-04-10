package io.bigoldbro.corex;


import io.bigoldbro.corex.annotation.BlockControl;

import java.util.Map;

/**
 * Created by Joshua on 2018/2/26.
 */
public interface Service extends ContextAware {

    String name();

    BlockControl bc();

    void start(Future<Void> completeFuture);

    void stop(Future<Void> completeFuture);

    void afterStart();

    void handleMsg(Msg msg);

    Map<String, Object> info();

}
