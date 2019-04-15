package io.bigoldbro.corex.module;

import io.bigoldbro.corex.annotation.Notice;
import io.bigoldbro.corex.annotation.Module;
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
    default void onUserLogin(String userId, int serverId, long loginTime) {
    }

    @Notice(topic = TopicDefine.USER_LOGOUT, role = ConstDefine.ROLE_SET1)
    default void onUserLogout(String userId, int serverId, long loginTime) {
    }

    @Notice(topic = TopicDefine.SERVER_UP, role = ConstDefine.ROLE_ALL)
    default void onServerUp(int serverId, long startTime) {
    }

    @Notice(topic = TopicDefine.SERVER_DOWN, role = ConstDefine.ROLE_ALL)
    default void onServerDown(int serverId, long startTime) {
    }

    @Notice(topic = TopicDefine.SERVER_INFO_UPDATE, role = ConstDefine.ROLE_LOCAL)
    default void onServerInfoUpdate(long updateTime) {
    }

    @Notice(topic = TopicDefine.KICK, role = ConstDefine.ROLE_GATEWAY)
    default void kick(List<String> userIds, int code, String msg) {
    }
}
