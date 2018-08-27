package corex.game.impl;

import corex.core.exception.CoreException;
import corex.game.GameInstance;
import corex.game.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Joshua on 2018/3/28.
 */
public abstract class AbstractGameInstance implements GameInstance {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final AbstractGame game;
    protected final String id;
    protected final long createTime;
    protected final List<Player> players;
    protected final int size;

    private int phase;

    public AbstractGameInstance(AbstractGame game, String id, List<Player> players, int minimum) {
        this.game = game;
        this.id = id;
        this.createTime = System.currentTimeMillis();
        if (players == null || players.size() < minimum) {
            throw new CoreException("游戏人数不足");
        }
        this.players = Collections.unmodifiableList(players);
        this.size = players.size();
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
    public List<Player> players() {
        return players;
    }

    protected Player player(int index) {
        return players.get(index % size);
    }

    @Override
    public int phase() {
        return phase;
    }

    @Override
    public void gotoPhaseNow(int phase) {
        game.setTimer(id, 0, () -> updatePhase(phase));
    }

    @Override
    public void gotoPhase(int phase, long delay) {
        game.setTimer(id, delay, () -> updatePhase(phase));
    }

    protected void cancelPhaseEvent() {
        game.cancelTimeEvent(id);
    }

    protected abstract boolean checkPhaseValid(int oldPhase, int newPhase);

    private void updatePhase(int phase) {
        int oldPhase = phase();
        if (!checkPhaseValid(oldPhase, phase)) {
            logger.warn("阶段转移错误, {} -> {}", oldPhase, phase);
            return;
        }
        this.phase = phase;
        onPhase(phase);
    }

    @Override
    public void start() {
        if (!game.addGame(this)) {
            throw new CoreException("addGame失败, id:" + id());
        }

        for (Player player : players) {
            player.setGameInstance(this);
        }
        onStart();
    }

    @Override
    public void end() {
        cancelPhaseEvent();
        onEnd();
        for (Player player : players) {
            player.setGameInstance(null);
        }
        game.removeGame(this);
    }

    protected List<String> playerIds() {
        List<String> uids = new ArrayList<>(size);
        for (Player p : players) {
            uids.add(p.userId());
        }
        return uids;
    }
}
