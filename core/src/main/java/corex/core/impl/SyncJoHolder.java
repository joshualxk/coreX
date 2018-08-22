package corex.core.impl;

import corex.core.AsyncResult;
import corex.core.Handler;
import corex.core.JoHolder;
import corex.core.json.JsonObject;

/**
 * Created by Joshua on 2018/8/23
 */
public class SyncJoHolder implements JoHolder {

    private final JsonObject jo;

    public SyncJoHolder() {
        jo = new JsonObject();
    }

    public SyncJoHolder(JsonObject jo) {
        this.jo = jo;
    }

    @Override
    public JsonObject jo() {
        return jo;
    }

    @Override
    public boolean isSync() {
        return true;
    }

    @Override
    public void addListener(Handler<AsyncResult<JoHolder>> handler) {
        throw new UnsupportedOperationException("addListener");
    }
}
