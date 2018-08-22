package corex.game.impl;

import corex.core.Context;
import corex.core.define.ConstDefine;
import corex.core.define.ExceptionDefine;
import corex.core.model.Auth;
import corex.core.model.Broadcast;
import corex.game.Xpusher;

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

        public AuthAndBroadcast(Auth auth) {
            this.auth = auth;
        }
    }
}
