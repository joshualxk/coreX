package io.bigoldbro.corex;

/**
 * Created by Joshua on 2018/2/26.
 */
public interface AsyncResult<T> {

    T result();

    Throwable cause();

    boolean succeeded();

    boolean failed();

}
