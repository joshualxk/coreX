package corex.game;

import corex.core.Joable;

/**
 * Created by Joshua on 2018/3/27.
 */
public interface Room extends Joable {

    int id();

    boolean isFull();

    boolean hasGameBegun();

    int num();

    void addPlayer(RoomPlayer player);

    void removePlayer(RoomPlayer player);

    String roomChannel();

    void reset();

    // 进入房间
    void onPlayerEnter(RoomPlayer roomPlayer);

    // 离开房间
    void onPlayerLeave(RoomPlayer roomPlayer);

    // 玩家上线
    void onPlayerOnline(RoomPlayer roomPlayer);

    // 玩家掉线
    void onPlayerOffline(RoomPlayer roomPlayer);

    // 玩家在游戏中
    void onPlayerPresent(RoomPlayer roomPlayer);

    // 玩家不在游戏中
    void onPlayerAbsent(RoomPlayer roomPlayer);

    // 玩家准备
    void onPlayerPrepared(RoomPlayer roomPlayer);

    // 玩家取消准备
    void onPlayerCancelPrepared(RoomPlayer roomPlayer);
}
