package io.bigoldbro.corex;

import io.bigoldbro.corex.model.Payload;

/**
 * Created by Joshua on 2018/2/26.
 */
public interface MsgHandler {

    void onMsgSent(long id, Handler<AsyncResult<Payload>> handler);

    void onMsgReply(long id, AsyncResult<Payload> resp);

    void removeExpireMsg();
}
