package corex.demo;

import corex.core.json.JsonObject;
import corex.game.impl.AbstractRoomPlayer;

/**
 * Created by Joshua on 2018/3/27.
 */
public class DemoGamePlayer extends AbstractRoomPlayer {

    public DemoGamePlayer(String userId, String nickName, String icon, boolean isRobot, boolean online, boolean present, boolean prepared) {
        super(userId, nickName, icon, isRobot, online, present, prepared);
    }

    @Override
    public JsonObject toJo() {
        JsonObject jo = new JsonObject();
        jo.put("nickName", nickName());
        jo.put("icon", icon());
        jo.put("seat", seat());
        jo.put("isA", isPrepared());
        jo.put("isB", isPresent());
        return jo;
    }
}
