package corex.game;

/**
 * Created by Joshua on 2018/3/23.
 */
public interface RoomPlayer extends Player {

    GameInstance gameInstance();

    Room room();

    boolean isPresent();

    boolean setPresent(boolean present);

    boolean isPrepared();

    boolean setPrepared(boolean prepared, boolean propagate);

    int seat();

    void setSeat(int seat);

    void setGameInstance(GameInstance gameInstance);

    void setRoom(Room room);

    void enterRoom(Room room);

    void leaveRoom();

    void prepare();

    void cancelPrepare();

    void onPresent();

    void onAbsent();

    void onPrepared();

    void onCancelPrepared();
}
