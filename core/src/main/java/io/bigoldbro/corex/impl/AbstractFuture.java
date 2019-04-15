package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.AsyncResult;
import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Joshua on 2019/04/17.
 */
public abstract class AbstractFuture<T> implements Future<T> {

    final static Logger logger = LoggerFactory.getLogger(AbstractFuture.class);

    protected void doHandler(Handler<AsyncResult<T>> handler) {
        try {
            handler.handle(this);
        } catch (Throwable t) {
            logger.debug("error", t);
        }
    }
}
