package corex.core.impl;

import corex.core.Connection;
import corex.core.Session;

/**
 * Created by Joshua on 2018/3/19.
 */
public class DefaultSession implements Session {

    private final String userId;
    private final Connection connection;
    private final long loginTime;

    public DefaultSession(String userId, Connection connection, long loginTime) {
        this.userId = userId;
        this.connection = connection;
        this.loginTime = loginTime;
    }

    @Override
    public String userId() {
        return userId;
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public long loginTime() {
        return loginTime;
    }
}
