package corex.core.impl;

import corex.core.AsyncResult;
import corex.core.Handler;
import corex.core.JoHolder;
import corex.core.json.JsonObject;

/**
 * Created by Joshua on 2018/8/23
 */
public class AsyncJoHolder extends FutureImpl<JoHolder> implements JoHolder {

    @Override
    public JsonObject jo() {
        throw new UnsupportedOperationException("jo");
    }

    @Override
    public boolean isSync() {
        return false;
    }

    @Override
    public void addListener(Handler<AsyncResult<JoHolder>> handler) {
        setHandler(handler);
    }
}
