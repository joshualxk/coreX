package corex.core.model;

import corex.core.Joable;
import corex.core.define.ConstDefine;
import corex.core.json.JsonObject;
import corex.core.utils.CoreXUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Joshua on 2018/8/22
 */
public class Broadcast implements Joable {

    public static final int T = 2;

    private final boolean internal;
    private final int role;
    private final List<String> channels;
    private final List<String> userIds;
    private final Push push;

    private Broadcast(boolean internal, int role, List<String> channels, List<String> userIds, Push push) {
        this.internal = internal;
        this.role = role;
        this.channels = channels == null ? new LinkedList<>() : channels;
        this.userIds = userIds == null ? new LinkedList<>() : userIds;
        this.push = push;
    }

    public static Broadcast newInternalBroadcast(int role, String topic, JsonObject body) {
        Push push = Push.newPush(topic, CoreXUtil.sysTime(), body);
        return new Broadcast(true, role, null, null, push);
    }

    public static Broadcast newCsUsBroadcast(List<String> channels, List<String> userIds, String topic, JsonObject body) {
        Push push = Push.newPush(topic, CoreXUtil.sysTime(), body);
        channels = channels == null ? new LinkedList<>() : channels;
        userIds = userIds == null ? new LinkedList<>() : userIds;
        return new Broadcast(false, ConstDefine.ROLE_GATEWAY, channels, userIds, push);
    }

    public static Broadcast newGroupBroadcast(String channel, List<String> userIds, String topic, JsonObject body) {
        return newCsUsBroadcast(Collections.singletonList(channel), userIds, topic, body);
    }

    public static Broadcast newSingleBroadcast(String channel, String userId, String topic, JsonObject body) {
        return newCsUsBroadcast(Collections.singletonList(channel), Collections.singletonList(userId), topic, body);
    }

    public boolean isInternal() {
        return internal;
    }

    public int getRole() {
        return role;
    }

    public List<String> getChannels() {
        return channels;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public Push getPush() {
        return push;
    }

    public static Broadcast fromJo(JsonObject jo) throws Exception {
        boolean internal = jo.getBoolean("itn");
        int role = jo.getInteger("role");
        List<String> channels = jo.getJsonArray("chs").getList();
        List<String> userIds = jo.getJsonArray("uIds").getList();
        Push push = Push.fromJo(jo.getJsonObject("p"));
        return new Broadcast(internal, role, channels, userIds, push);
    }

    @Override
    public JsonObject toJo() {
        return new JsonObject()
                .put("itn", internal)
                .put("role", role)
                .put("chs", channels)
                .put("uIds", userIds)
                .put("p", push.toJo());
    }
}
