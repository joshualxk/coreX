package corex.core.exception;

/**
 * Created by Joshua on 2018/2/23.
 */
public class BizExceptionBuilder implements BizEx {

    private final int code;
    private final String msg;

    public BizExceptionBuilder(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public BizException build() {
        return new BizException(code, msg);
    }

}
