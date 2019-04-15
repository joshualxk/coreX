package io.bigoldbro.corex.game;

import io.bigoldbro.corex.proto.Base;

/**
 * Created by Joshua on 2018/3/27.
 */
public interface Xpusher {

    void before(Base.Auth auth);

    String userId();

    void addBroadcast(Base.Broadcast broadcast);

    void after(boolean success);
}
