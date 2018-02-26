package corex.game.impl;

import corex.core.exception.CoreException;
import corex.game.GameInstance;
import corex.game.Room;
import corex.game.RoomPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by Joshua on 2018/3/28.
 */
public abstract class AbstractGameInstance implements GameInstance {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final AbstractGame game;
    protected final AbstractRoom room;
    protected final String id;
    protected final long createTime;
    protected final List<RoomPlayer> players;
    protected final int size;

    private int phase;

    public AbstractGameInstance(AbstractGame game, AbstractRoom room, String id, List<RoomPlayer> players, int minimum) {
        this.game = game;
        this.room = room;
        this.id = id;
        this.createTime = System.currentTimeMillis();
        if (players == null || players.size() < minimum) {
            throw new CoreException("游戏人数不足");
        }
        this.players = Collections.unmodifiableList(players);
        this.size = players.size();
    }

    @Override
    public Room room() {
        return room;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public long createTime() {
        return createTime;
    }

    @Override
    public List<RoomPlayer> players() {
        return players;
    }

    protected RoomPlayer player(int index) {
        return players.get(index % size);
    }

    @Override
    public int phase() {
        return phase;
    }

    @Override
    public boolean gotoPhase(int phase, long delay) {
        if (delay == 0) {
            updatePhase(phase);
        } else {
            game.setTimeEvent(id, delay, () -> updatePhase(phase));
        }
        return true;
    }

    private void updatePhase(int phase) {
        this.phase = phase;
        onPhase(phase);
    }

    @Override
    public void start() {
        if (!game.addGame(this)) {
            throw new CoreException("addGame失败, id:" + id());
        }

        room.setGameInstance(this);
        for (RoomPlayer player : players) {
            player.setGameInstance(this);
        }
        onStart();
    }

    @Override
    public void end() {
        try {
            onEnd();
        } finally {
            room.reset();
        }
    }
}
