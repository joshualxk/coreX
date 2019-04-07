package io.bigoldbro.corex.exception;

/**
 * Created by Joshua on 2018/2/26.
 */
public class NoStackTraceThrowable extends Throwable {

    public NoStackTraceThrowable(String message) {
        super(message, null, false, false);
    }
}
