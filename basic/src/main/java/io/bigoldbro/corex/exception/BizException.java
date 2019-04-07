package io.bigoldbro.corex.exception;

/**
 * Created by Joshua on 2018/2/23.
 */
public class BizException extends RuntimeException implements BizEx {

    private final int code;
    private final String message;

    public BizException(int code, String message) {
        super(message, null, false, false);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
