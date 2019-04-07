package io.bigoldbro.corex;

/**
 * Created by Joshua on 2019/4/7
 */
public interface Callback<T> {

    T sync() throws Exception;

    void onResult(Handler<AsyncResult<T>> handler);
}
