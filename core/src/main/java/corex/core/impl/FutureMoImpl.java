package corex.core.impl;

import corex.core.AsyncResult;
import corex.core.FutureMo;
import corex.core.Handler;
import corex.core.Mo;

/**
 * Created by Joshua on 2018/3/14.
 */
public class FutureMoImpl extends MoImpl implements FutureMo {

    @Override
    public void addListener(Handler<AsyncResult<Mo>> handler) {
        throw new UnsupportedOperationException("addListener");
    }
}
