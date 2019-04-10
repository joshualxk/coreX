package io.bigoldbro.corex.module;

import io.bigoldbro.corex.annotation.Notice;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Param;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;
import io.bigoldbro.corex.define.TopicDefine;

import java.util.List;

/**
 * Created by Joshua on 2018/3/16.
 */
@Module(address = ServiceNameDefine.BROADCAST)
public interface BroadcastModule {

    @Notice(topic = TopicDefine.USER_LOGIN, role = ConstDefine.ROLE_SET1)
    default void onUserLogin(@Param("1") String userId, @Param("2") int serverId, @Param("3") long loginTime) {
    }

    @Notice(topic = TopicDefine.USER_LOGOUT, role = ConstDefine.ROLE_SET1)
    default void onUserLogout(@Param("1") String userId, @Param("2") int serverId, @Param("3") long loginTime) {
    }

    @Notice(topic = TopicDefine.SERVER_UP, role = ConstDefine.ROLE_ALL)
    default void onServerUp(@Param("1") int serverId, @Param("2") long startTime) {
    }

    @Notice(topic = TopicDefine.SERVER_DOWN, role = ConstDefine.ROLE_ALL)
    default void onServerDown(@Param("1") int serverId, @Param("2") long startTime) {
    }

    @Notice(topic = TopicDefine.SERVER_INFO_UPDATE, role = ConstDefine.ROLE_LOCAL)
    default void onServerInfoUpdate(@Param("1") long updateTime) {
    }

    @Notice(topic = TopicDefine.KICK, role = ConstDefine.ROLE_GATEWAY)
    default void kick(@Param("1") List<String> userIds, @Param("2") int code, @Param("3") String msg) {
    }
}
