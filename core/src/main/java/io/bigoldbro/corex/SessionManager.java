package io.bigoldbro.corex;

/**
 * Created by Joshua on 2018/3/16.
 */
public interface SessionManager extends ContextAware {

    void register(Connection conn, String channel);

    void unregister(Connection conn);

    void login(Connection conn, String id);

    String logout(Connection conn);

    void leave(Connection conn);

    boolean hasLogin(Connection conn);

    String userId(Connection conn);
}
