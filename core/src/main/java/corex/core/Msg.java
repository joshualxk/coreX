package corex.core;

import corex.core.model.Payload;
import corex.core.utils.CoreXUtil;

/**
 * Created by Joshua on 2018/2/26.
 */
public interface Msg {

    long id();

    // 解引用
    Payload detach();

    void reply(AsyncResult<Payload> resp);

    default boolean needReply() {
        return CoreXUtil.needReply(id());
    }
}
