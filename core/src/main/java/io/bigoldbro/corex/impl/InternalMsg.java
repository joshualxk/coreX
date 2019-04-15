package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.AsyncResult;
import io.bigoldbro.corex.Context;
import io.bigoldbro.corex.Msg;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.proto.Base;

/**
 * Created by Joshua on 2018/2/26.
 */
class InternalMsg implements Msg {

    private final Context context;
    private final long id;
    private Base.Payload body;

    public InternalMsg(Context context, long id, Base.Payload body) {
        this.context = context;
        this.id = id;
        this.body = body;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public Base.Payload detach() {
        // TODO optimize this
        Base.Payload obj;
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
    public void reply(AsyncResult<Base.Payload> resp) {
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
