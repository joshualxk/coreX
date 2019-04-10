package io.bigoldbro.corex.exception;

/**
 * Created by Joshua on 2018/2/23.
 */
public class CodecException extends RuntimeException {

    public CodecException() {
    }

    public CodecException(String message) {
        super(message);
    }

    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }
}
