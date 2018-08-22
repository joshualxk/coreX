package corex.core.exception;

/**
 * Created by Joshua on 2018/2/26.
 * 使用不当导致的错误
 */
public class CoreException extends RuntimeException {

    public CoreException() {
    }

    public CoreException(String message) {
        super(message);
    }

    public CoreException(String message, Throwable cause) {
        super(message, cause);
    }

}
