package corex.game;

import java.util.List;

/**
 * Created by Joshua on 2018/3/23.
 */
public interface GameInstance {

    String id();

    long createTime();

    List<RoomPlayer> players();

    Room room();

    int phase();

    boolean gotoPhase(int phase, long delay);

    void onStart();

    void onPhase(int phase);

    void onEnd();

    void start();

    void end();
}
