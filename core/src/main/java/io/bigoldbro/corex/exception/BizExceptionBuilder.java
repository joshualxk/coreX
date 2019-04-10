package io.bigoldbro.corex.exception;

/**
 * Created by Joshua on 2018/2/23.
 */
public class BizExceptionBuilder implements BizEx {

    private final int code;
    private final String message;

    public BizExceptionBuilder(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public BizException build() {
        return new BizException(code, message);
    }

}
