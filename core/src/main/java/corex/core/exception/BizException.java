package corex.core.exception;

/**
 * Created by Joshua on 2018/2/23.
 */
public class BizException extends RuntimeException implements BizEx {

    private final int code;
    private final String msg;

    public BizException(int code, String msg) {
        super(msg, null, false, false);
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
