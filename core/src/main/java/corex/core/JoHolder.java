package corex.core;

import corex.core.impl.AsyncJoHolder;
import corex.core.impl.SyncJoHolder;
import corex.core.json.JsonObject;

/**
 * Created by Joshua on 2018/8/23
 */
public interface JoHolder {

    JsonObject jo();

    boolean isSync();

    void addListener(Handler<AsyncResult<JoHolder>> handler);

    static JoHolder newSync() {
        return new SyncJoHolder();
    }

    static JoHolder newSync(JsonObject jo) {
        return new SyncJoHolder(jo);
    }

    static AsyncJoHolder newAsync() {
        return new AsyncJoHolder();
    }

    static AsyncJoHolder newFailedAsync(Throwable t) {
        AsyncJoHolder asyncJoHolder = new AsyncJoHolder();
        asyncJoHolder.fail(t);
        return asyncJoHolder;
    }

}
