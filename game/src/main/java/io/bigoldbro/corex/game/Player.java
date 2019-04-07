package io.bigoldbro.corex.game;

/**
 * Created by Joshua on 2018/3/23.
 */
public interface Player extends Joable {

    String userId();

    String nickName();

    String icon();

    boolean isRobot();

    boolean isOnline();

    /**
     * 更新在线状态
     *
     * @param online
     * @return 返回true表示状态有变化
     */
    boolean setOnline(boolean online);

    void onOnlineChange(boolean online);

    boolean setState(int state);

    int state();

    void onStateChange(int oldState, int newState);

    GameInstance gameInstance();

    void setGameInstance(GameInstance gameInstance);

    int index();
}
