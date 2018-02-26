package corex.core;

import corex.core.utils.CoreXUtil;

/**
 * Created by Joshua on 2018/2/26.
 */
public interface Msg {

    long id();

    // 解引用
    Object detach();

    void reply(AsyncResult<Object> resp);

    default boolean needReply() {
        return CoreXUtil.needReply(id());
    }
}
