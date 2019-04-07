package io.bigoldbro.corex.model;

import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.json.Joable;
import io.bigoldbro.corex.json.JsonObject;
import io.bigoldbro.corex.utils.CoreXUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Joshua on 2018/8/22
 */
public class Broadcast implements Joable {

    public static final int T = 2;

    private boolean internal;
    private int role;
    private List<String> channels;
    private List<String> userIds;
    private Push push;

    private Broadcast(boolean internal, int role, List<String> channels, List<String> userIds, Push push) {
        this.internal = internal;
        this.role = role;
        this.channels = channels == null ? new LinkedList<>() : channels;
        this.userIds = userIds == null ? new LinkedList<>() : userIds;
        this.push = push;
    }

    public Broadcast() {
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

    @Override
    @SuppressWarnings("unchecked")
    public void readFrom(JsonObject jo) {
        internal = jo.getBoolean("itn");
        role = jo.getInteger("role");
        channels = jo.getJsonArray("chs").getList();
        userIds = jo.getJsonArray("uIds").getList();
        push = jo.getJoable("p", Push.class);
    }

    @Override
    public void writeTo(JsonObject jo) {
        jo.put("itn", internal)
                .put("role", role)
                .put("chs", channels)
                .put("uIds", userIds)
                .put("p", push);
    }
}
