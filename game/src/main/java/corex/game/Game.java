package corex.game;

/**
 * Created by Joshua on 2018/3/26.
 */
public interface Game {

    int gameId();

    boolean isClosing();

    // 玩家数量上限,-1无上限
    int playerLimit();

    // 多少名玩家正在玩
    int playerNum();

    // 正在进行多少场游戏
    int instanceNum();

    // 玩家是否在这个游戏
    boolean isPlayerJoined(String userId);

    void onInit();

    void onDestroy();

}
