package corex.core.impl;

import corex.core.AsyncResult;
import corex.core.Context;
import corex.core.Msg;
import corex.core.exception.CoreException;
import corex.core.model.Payload;

/**
 * Created by Joshua on 2018/2/26.
 */
class InternalMsg implements Msg {

    private final Context context;
    private final long id;
    private Payload body;

    public InternalMsg(Context context, long id, Payload body) {
        this.context = context;
        this.id = id;
        this.body = body;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public Payload detach() {
        Payload obj;
        synchronized (this) {
            if (body == null) {
                throw new CoreException("Body has already detached");
            }
            obj = body;
            body = null;
        }
        return obj;
    }

    @Override
    public void reply(AsyncResult<Payload> resp) {
        // TODO 验证不在同一个context
        if (needReply()) {
            context.coreX().onMsgReply(id, resp);
        } else {
            if (resp.succeeded()) {
                context.coreX().sendMessage(context.name(), resp.result(), null);
            }
        }
    }
}
