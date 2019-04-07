package io.bigoldbro.corex.demo;

import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.define.TopicDefine;
import io.bigoldbro.corex.model.Broadcast;
import io.bigoldbro.corex.utils.RandomUtil;
import io.bigoldbro.corex.game.Player;
import io.bigoldbro.corex.game.impl.AbstractGame;
import io.bigoldbro.corex.game.impl.AbstractGameInstance;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Joshua on 2018/3/28.
 */
public class DemoGameInstance extends AbstractGameInstance {

    public static final int PLAYER_NUM = 2;

    private static final int SCALE = 3;
    private static final int ROUND_TIME = 30 * 1000;    // 30s;

    private int curPlayer;
    private int winner;
    private int[] grids = new int[SCALE * SCALE];

    private static final int GRID_EMPTY = 0;
    private static final int GRID_P1 = 1;
    private static final int GRID_P2 = 2;

    DemoGameInstance(AbstractGame game, String id, List<Player> players) {
        super(game, id, players, PLAYER_NUM);
    }

    @Override
    public void pushDetailedInfo(Player player) {
        JsonObjectImpl jo = new JsonObjectImpl();
        jo.put("t", phase());
        jo.put("cur", curPlayer);
        jo.put("u", player.index());
        jo.put("ps", Joable.list2Ja(players));
        jo.put("g", Arrays.asList(grids));

        game.addBroadcast(Broadcast.newSingleBroadcast(id(), player.userId(), TopicDefine.GAME_INFO, jo));
    }

    @Override
    public JoHolder play(Player player, JsonObjectImpl op) {
        if (player.index() != curPlayer) {
            throw ExceptionDefine.NOT_UR_TURN.build();
        }

        int t = op.getIntegerParam("t");
        if (t < 0 || t >= SCALE * SCALE) {
            throw ExceptionDefine.PARAM_ERR.build();
        }
        if (grids[t] != GRID_EMPTY) {
            throw ExceptionDefine.newException("格子不为空");
        }
        grids[t] = player.index() == 0 ? GRID_P1 : GRID_P2;

        tryNextRound();

        return JoHolder.newSync();
    }

    private Player checkWinner() {
        int c1;
        int c2;

        for (int i = 0; i < SCALE; ++i) {
            c1 = 0;
            c2 = 0;
            for (int j = 0; j < SCALE; ++j) {
                if (grids[i * SCALE + j] == GRID_P1) {
                    c2++;
                } else if (grids[i * SCALE + j] == GRID_P2) {
                    c2--;
                }

                if (grids[j * SCALE + i] == GRID_P1) {
                    c1++;
                } else if (grids[j * SCALE + i] == GRID_P2) {
                    c1--;
                }
            }

            if (c1 == SCALE || c2 == SCALE) {
                return player(0);
            } else if (c1 == -SCALE || c2 == -SCALE) {
                return player(1);
            }
        }

        c1 = 1;
        c2 = 1;
        for (int i = 1; i < SCALE; ++i) {
            if (grids[(SCALE + 1) * i] == grids[(SCALE + 1) * (i - 1)]) {
                c1++;
            }
            if (grids[(SCALE - 1) * i] == grids[(SCALE - 1) * (i - 1)]) {
                c2++;
            }
        }

        if (c1 == SCALE && grids[0] != GRID_EMPTY) {
            return player(grids[0] == GRID_P1 ? 0 : 1);
        }
        if (c2 == SCALE && grids[SCALE - 1] != GRID_EMPTY) {
            return player(grids[SCALE - 1] == GRID_P1 ? 0 : 1);
        }
        return null;
    }

    private void tryNextRound() {
        Player player = checkWinner();
        if (player != null) {
            winner = player.index();
            end();
        } else {
            gotoPhaseNow(phase() + 1);
        }
    }

    @Override
    public void onStart() {
        logger.debug("onStart");

        for (Player player : players) {
            player.setState(DemoGameImpl.STATE_PLAYING);
        }

        curPlayer = RandomUtil.nextInt(size);
        gotoPhase(phase() + 1, ROUND_TIME);

        for (Player p : players) {
            pushDetailedInfo(p);
        }

    }

    @Override
    protected boolean checkPhaseValid(int oldPhase, int newPhase) {
        return newPhase == oldPhase + 1;
    }

    @Override
    public void onPhase(int phase) {
        logger.debug("onPhase, {}", phase);

        curPlayer = ++curPlayer % size;

        JsonObjectImpl jo = new JsonObjectImpl();
        jo.put("t", phase());
        jo.put("g", Arrays.asList(grids));
        game.addBroadcast(Broadcast.newGroupBroadcast(id(), playerIds(), TopicDefine.GAME_INFO, jo));

        gotoPhase(phase + 1, ROUND_TIME);
    }

    @Override
    public void onEnd() {
        logger.debug("onEnd");

        for (Player player : players) {
            player.setState(DemoGameImpl.STATE_IDLE);
        }

        JsonObjectImpl jo = new JsonObjectImpl();
        jo.put("t", phase());
        jo.put("g", Arrays.asList(grids));
        jo.put("winner", winner);
        game.addBroadcast(Broadcast.newGroupBroadcast(id(), playerIds(), TopicDefine.GAME_INFO, jo));
    }
}
