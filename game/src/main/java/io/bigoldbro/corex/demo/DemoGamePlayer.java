package io.bigoldbro.corex.demo;

import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.game.impl.AbstractPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Joshua on 2018/3/27.
 */
public class DemoGamePlayer extends AbstractPlayer {

    protected static final Logger logger = LoggerFactory.getLogger(DemoGamePlayer.class);

    private final DemoGameImpl demoGame;

    DemoGamePlayer(DemoGameImpl demoGame, String userId, String nickName, String icon, boolean isRobot, boolean online, int state) {
        super(userId, nickName, icon, isRobot, online, state);
        this.demoGame = demoGame;
    }

    @Override
    public void onOnlineChange(boolean online) {
        if (online) {
            if (state() == DemoGameImpl.STATE_PLAYING) {
                // TODO 通知其他玩家上线状态
            }
        } else {
            if (state() != DemoGameImpl.STATE_PLAYING) {
                this.demoGame.cancelMatch(userId());
                this.demoGame.removePlayer(this);
            } else {
                // TODO 通知其他玩家下线状态
            }
        }
    }

    @Override
    public void onStateChange(int oldState, int newState) {
        logger.info("玩家 {} 状态 {} -> {}", userId(), oldState, newState);
    }

    @Override
    public JsonObjectImpl toJo() {
        return new JsonObjectImpl()
                .put("nn", nickName())
                .put("ic", icon())
                .put("isO", isOnline());
    }
}
