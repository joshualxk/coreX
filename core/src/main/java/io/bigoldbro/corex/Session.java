package io.bigoldbro.corex;

/**
 * Created by Joshua on 2018/3/19.
 */
public interface Session {

    String userId();

    Connection connection();

    long loginTime();
}
