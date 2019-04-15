package io.bigoldbro.corex;

import io.bigoldbro.corex.proto.Base;

/**
 * Created by Joshua on 2018/2/26.
 */
public interface MsgHandler {

    void onMsgSent(long id, Handler<AsyncResult<Base.Payload>> handler);

    void onMsgReply(long id, AsyncResult<Base.Payload> resp);

    void removeExpireMsg();
}
