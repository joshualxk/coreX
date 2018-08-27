package corex.game.impl;

import corex.core.exception.CoreException;
import corex.game.GameInstance;
import corex.game.Player;

import java.util.List;

/**
 * Created by Joshua on 2018/3/26.
 */
public abstract class AbstractPlayer implements Player {

    private final String userId;
    private final String nickName;
    private final String icon;
    private final boolean isRobot;

    private GameInstance gameInstance;
    private int index;
    private boolean online;
    private int state;

    public AbstractPlayer(String userId, String nickName, String icon, boolean isRobot, boolean online, int state) {
        this.userId = userId;
        this.nickName = nickName;
        this.icon = icon;
        this.isRobot = isRobot;
        this.online = online;
        this.state = state;
    }

    @Override
    public String userId() {
        return userId;
    }

    @Override
    public String nickName() {
        return nickName;
    }

    @Override
    public String icon() {
        return icon;
    }

    @Override
    public boolean isRobot() {
        return isRobot;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public boolean setOnline(boolean online) {
        if (this.online != online) {
            this.online = online;
            onOnlineChange(online);
            return true;
        }
        return false;
    }

    @Override
    public boolean setState(int state) {
        if (this.state != state) {
            int oldState = this.state;
            this.state = state;
            onStateChange(oldState, state);
            return true;
        }
        return false;
    }

    @Override
    public int state() {
        return state;
    }

    @Override
    public GameInstance gameInstance() {
        return gameInstance;
    }

    @Override
    public void setGameInstance(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        index = -1;
        if (gameInstance != null) {
            List<Player> players = gameInstance.players();
            for (int i = 0; i < players.size(); ++i) {
                if (players.get(i).userId().equals(userId())) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                throw new CoreException("未找到玩家,id:" + userId());
            }
        }
    }

    @Override
    public int index() {
        return index;
    }
}
