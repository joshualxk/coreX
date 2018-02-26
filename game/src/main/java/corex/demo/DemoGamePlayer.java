package corex.demo;

import corex.core.Mo;
import corex.game.impl.AbstractRoomPlayer;

/**
 * Created by Joshua on 2018/3/27.
 */
public class DemoGamePlayer extends AbstractRoomPlayer {

    public DemoGamePlayer(String userId, String nickName, String icon, boolean isRobot, boolean online, boolean present, boolean prepared) {
        super(userId, nickName, icon, isRobot, online, present, prepared);
    }

    @Override
    public Mo toMo() {
        Mo mo = Mo.mo();
        mo.putString("nickName", nickName());
        mo.putString("icon", icon());
        mo.putInt("seat", seat());
        mo.putBoolean("isA", isPrepared());
        mo.putBoolean("isB", isPresent());
        return mo;
    }
}
