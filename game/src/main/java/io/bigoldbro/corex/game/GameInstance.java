package io.bigoldbro.corex.game;

import io.bigoldbro.corex.proto.Base;

import java.util.List;

/**
 * Created by Joshua on 2018/3/23.
 */
public interface GameInstance {

    String id();

    long createTime();

    List<Player> players();

    // 给某个玩家推送详细的游戏信息
    void pushDetailedInfo(Player player);

    Base.Body play(Player player, Base.Body op);

    int phase();

    void gotoPhaseNow(int phase);

    void gotoPhase(int phase, long delay);

    void onStart();

    void onPhase(int phase);

    void onEnd();

    void start();

    void end();
}
