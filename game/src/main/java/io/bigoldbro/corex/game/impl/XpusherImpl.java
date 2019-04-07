package io.bigoldbro.corex.game.impl;

import io.bigoldbro.corex.Context;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.model.Auth;
import io.bigoldbro.corex.model.Broadcast;
import io.bigoldbro.corex.game.Xpusher;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Joshua on 2018/3/21.
 */
public class XpusherImpl implements Xpusher {

    private static ThreadLocal<AuthAndBroadcast> threadLocal = new ThreadLocal<>();

    private final Context context;

    public XpusherImpl(Context context) {
        this.context = context;
    }

    @Override
    public void before(Auth auth) {
        threadLocal.set(new AuthAndBroadcast(auth));
    }

    @Override
    public String userId() {
        AuthAndBroadcast aab = threadLocal.get();
        if (aab == null || aab.auth.getType() != ConstDefine.AUTH_TYPE_CLIENT) {
            throw ExceptionDefine.NOT_LOGIN.build();
        }
        return aab.auth.getToken();
    }

    @Override
    public void addBroadcast(Broadcast broadcast) {
        if (broadcast == null) {
            return;
        }
        AuthAndBroadcast aab = threadLocal.get();
        if (aab != null) {
            aab.broadcasts.add(broadcast);
        }
    }

    @Override
    public void after(boolean success) {
        if (success) {
            AuthAndBroadcast aab = threadLocal.get();
            if (aab != null) {
                for (Broadcast broadcast : aab.broadcasts) {
                    context.coreX().broadcastMessage(broadcast);
                }
            }
        }
        threadLocal.remove();
    }

    static class AuthAndBroadcast {
        final Auth auth;
        final List<Broadcast> broadcasts = new LinkedList<>();

        AuthAndBroadcast(Auth auth) {
            this.auth = auth;
        }
    }
}
