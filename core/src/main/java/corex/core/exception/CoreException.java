package corex.core.exception;

/**
 * Created by Joshua on 2018/2/26.
 */
public class CoreException extends RuntimeException {

    public CoreException(String msg) {
        super(msg);
    }

    public CoreException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
