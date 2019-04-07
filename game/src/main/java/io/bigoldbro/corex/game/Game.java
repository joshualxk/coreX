package io.bigoldbro.corex.game;

/**
 * Created by Joshua on 2018/3/26.
 */
public interface Game {

    // 游戏id
    int gameId();

    // 游戏正在关闭
    boolean isClosing();

    // 玩家数量上限,-1无上限
    int playerLimit();

    // 多少名玩家在这个游戏
    int playerNum();

    // 正在进行多少场游戏
    int instanceNum();

    // 获取玩家状态，为null不在这个游戏中
    Player getPlayer(String userId);

    void onGameInit();

    void onGameDestroy();

}
