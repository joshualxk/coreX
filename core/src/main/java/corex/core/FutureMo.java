package corex.core;

import corex.core.impl.AsyncFutureMoImpl;
import corex.core.impl.FutureMoImpl;

/**
 * Created by Joshua on 2018/3/13.
 */
public interface FutureMo extends Mo {

    static FutureMo futureMo() {
        return new FutureMoImpl();
    }

    static AsyncFutureMo asyncFutureMo() {
        return new AsyncFutureMoImpl();
    }

    void addListener(Handler<AsyncResult<Mo>> handler);
}
