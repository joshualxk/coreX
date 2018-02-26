package corex.core;

/**
 * Created by Joshua on 2018/2/26.
 */
public interface MsgHandler {

    void onMsgSent(long id, Handler<AsyncResult<Object>> handler);

    void onMsgReply(long id, AsyncResult<Object> resp);

    void removeExpireMsg();
}
