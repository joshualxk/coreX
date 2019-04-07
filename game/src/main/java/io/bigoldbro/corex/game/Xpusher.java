package io.bigoldbro.corex.game;

import io.bigoldbro.corex.model.Auth;
import io.bigoldbro.corex.model.Broadcast;

/**
 * Created by Joshua on 2018/3/27.
 */
public interface Xpusher {

    void before(Auth auth);

    String userId();

    void addBroadcast(Broadcast broadcast);

    void after(boolean success);
}
