package io.bigoldbro.corex;

import io.bigoldbro.corex.proto.Base;
import io.bigoldbro.corex.utils.CoreXUtil;

/**
 * Created by Joshua on 2018/2/26.
 */
public interface Msg {

    long id();

    // 解引用
    Base.Payload detach();

    void reply(AsyncResult<Base.Payload> resp);

    default boolean needReply() {
        return CoreXUtil.needReply(id());
    }
}
