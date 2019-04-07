package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.Connection;
import io.bigoldbro.corex.Context;
import io.bigoldbro.corex.Session;
import io.bigoldbro.corex.SessionManager;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.exception.BizEx;
import io.bigoldbro.corex.model.Broadcast;
import io.bigoldbro.corex.utils.CoreXUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Joshua on 2018/3/16.
 */
public class SessionManagerImpl implements SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManagerImpl.class);

    private final BroadcastReceiver broadcastReceiver;
    private final Map<Connection, String> registeredChannels = new HashMap<>();
    private final Map<String, Set<Connection>> channelMaps = new HashMap<>();
    private final Map<String, Session> onlineUsers = new HashMap<>();
    private final Map<String, Long> logoutEvents = new HashMap<>();

    private Context context;

    public SessionManagerImpl() {
        broadcastReceiver = new InternalBroadcastReceiver();
    }

    @Override
    public void register(Connection conn, String channel) {
        unregister(conn);
        Set<Connection> connections;
        if (channelMaps.containsKey(channel)) {
            connections = channelMaps.get(channel);
        } else {
            connections = new HashSet<>();
            channelMaps.put(channel, connections);
        }
        registeredChannels.put(conn, channel);
        connections.add(conn);
    }

    @Override
    public void unregister(Connection conn) {
        String channel = registeredChannels.remove(conn);
        if (channel != null) {
            Set<Connection> connections = channelMaps.get(channel);
            if (connections != null) {
                connections.remove(conn);
            }
        }
    }

    @Override
    public void login(Connection conn, String id) {
        removeLogoutEvent(id);
        Session session = new DefaultSession(id, conn, System.currentTimeMillis());
        conn.setSession(session);
        Session oldSession = onlineUsers.put(id, session);
        if (oldSession != null) {
            killOldSession(oldSession);
        }

        coreX().broadcast().onUserLogin(id, coreX().serverId(), session.loginTime());
    }

    @Override
    public String logout(Connection conn) {
        Session session = logout0(conn);

        if (session != null) {
            coreX().broadcast().onUserLogout(session.userId(), coreX().serverId(), session.loginTime());
            return session.userId();
        }

        return null;
    }

    @Override
    public void leave(Connection conn) {
        unregister(conn);
        Session session = logout0(conn);

        if (session != null) {
            addLogoutEvent(session.userId(), session.loginTime());
        }
    }

    private Session logout0(Connection conn) {
        Session session = conn.session();

        if (session == null) {
            return null;
        }
        conn.setSession(null);
        onlineUsers.remove(session.userId());
        return session;
    }

    private void killOldSession(Session session) {
        BizEx bizEx = ExceptionDefine.DUPLICATE_LOGIN;
        session.connection().write(CoreXUtil.kickMessage(bizEx.getCode(), bizEx.getMessage()));
        logout0(session.connection());
    }

    private void addLogoutEvent(String userId, long loginTime) {
        long tid = coreX().setTimer(3000, v -> {
            coreX().broadcast().onUserLogout(userId, coreX().serverId(), loginTime);
        });
        logoutEvents.put(userId, tid);
    }

    private void removeLogoutEvent(String userId) {
        Long tid = logoutEvents.remove(userId);
        if (tid != null) {
            coreX().cancelTimer(tid);
        }
    }

    @Override
    public boolean hasLogin(Connection conn) {
        return conn.session() != null;
    }

    @Override
    public String userId(Connection conn) {
        return conn.session().userId();
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public void init(Context context) {
        this.context = context;
        this.broadcastReceiver.init(context);
    }

    private class InternalBroadcastReceiver extends BroadcastReceiver {

        public InternalBroadcastReceiver() {
            super(true, true);
        }

        @Override
        protected int broadcast2Users(Broadcast broadcast) {
            Set<Connection> notifyConns = new HashSet<>();

            for (String channel : broadcast.getChannels()) {
                Set<Connection> set = channelMaps.get(channel);
                if (set == null) {
                    continue;
                }
                notifyConns.addAll(set);
            }

            for (String userId : broadcast.getUserIds()) {
                Session session = onlineUsers.get(userId);
                if (session == null) {
                    continue;
                }
                notifyConns.add(session.connection());
            }

            for (Connection conn : notifyConns) {
                conn.write(broadcast.getPush());
            }

            return notifyConns.size();
        }

        @Override
        public void onUserLogin(String userId, int serverId, long loginTime) {
            Session session = onlineUsers.get(userId);
            if (session != null) {
                if (loginTime > session.loginTime()) {
                    killOldSession(session);
                }
            } else {
                removeLogoutEvent(userId);
            }
        }

        @Override
        public void onUserLogout(String userId, int serverId, long loginTime) {

        }

        @Override
        public void kick(List<String> userIds, int code, String msg) {
            for (String userId : userIds) {
                Session session = onlineUsers.get(userId);
                if (session != null) {
                    session.connection().write(CoreXUtil.kickMessage(code, msg));
                    logout(session.connection());
                }
            }
        }
    }
}
