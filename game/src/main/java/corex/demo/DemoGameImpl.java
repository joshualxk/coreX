package corex.demo;

import corex.core.JoHolder;
import corex.core.define.ExceptionDefine;
import corex.core.json.JsonObject;
import corex.game.GameInstance;
import corex.game.Player;
import corex.game.impl.AbstractGame;

import java.util.*;

/**
 * Created by Joshua on 2018/3/26.
 */
public class DemoGameImpl extends AbstractGame implements DemoGame {

    public static final int STATE_IDLE = 0;
    public static final int STATE_MATCHING = 1;
    public static final int STATE_PLAYING = 2;

    private static int GAME_INSTANCE_COUNTER = 0;

    private LinkedHashMap<String, Player> matchingQueue = new LinkedHashMap<>();

    public DemoGameImpl() {
        super(DemoGame.class);
    }

    @Override
    public int gameId() {
        return 132123;
    }

    @Override
    public int playerLimit() {
        return 10;
    }

    @Override
    public void onGameInit() {
        setPeriodic("init", 4000, this::tryFindMatch);
    }

    @Override
    public void onGameDestroy() {
        cancelTimeEvent("init");
    }

    @Override
    public JoHolder connect() {
        JoHolder ret = JoHolder.newSync();
        ret.jo().put("code", 123);
        return ret;
    }

    @Override
    public JoHolder match() {
        String userId = userId();

        JoHolder ret = JoHolder.newSync();
        JsonObject jo = ret.jo();

        Player player = getPlayer(userId);
        if (player == null) {
            checkLimit();
            player = new DemoGamePlayer(this, userId, "player-" + userId, null, false, true, STATE_MATCHING);
            addPlayer(player);
        } else if (player.state() != STATE_IDLE) {
            throw ExceptionDefine.ALREADY_IN_GAME.build();
        }

        // 匹配
        joinMatch(player);
        jo.put("u", player.toJo());

        return ret;
    }

    @Override
    public JoHolder cancelMatch() {

        Player player = ensurePlayer(userId());

        if (player.state() == STATE_MATCHING) {
            cancelMatch(player.userId());
        }

        return JoHolder.newSync();
    }

    private static String gameInstanceId() {
        return "demo-" + GAME_INSTANCE_COUNTER++;
    }

    private void tryFindMatch() {
        while (matchingQueue.size() >= DemoGameInstance.PLAYER_NUM) {
            Iterator<Map.Entry<String, Player>> iterator = matchingQueue.entrySet().iterator();
            List<Player> players = new ArrayList<>(DemoGameInstance.PLAYER_NUM);
            for (int i = 0; i < DemoGameInstance.PLAYER_NUM; ++i) {
                players.add(iterator.next().getValue());
                iterator.remove();
            }

            DemoGameInstance gameInstance = new DemoGameInstance(this, gameInstanceId(), players);
            gameInstance.start();
        }
    }

    private void joinMatch(Player player) {
        matchingQueue.put(player.userId(), player);
        player.setState(STATE_MATCHING);
        logger.info("玩家 {} 加入匹配", player.nickName());
    }

    public void cancelMatch(String userId) {
        Player player = matchingQueue.remove(userId);
        if (player != null) {
            player.setState(STATE_IDLE);
            logger.info("玩家 {} 退出匹配", player.nickName());
        }
    }

    @Override
    public JoHolder play(JsonObject op) {
        Player player = ensurePlayer(userId());

        GameInstance gameInstance = player.gameInstance();
        if (gameInstance == null) {
            throw ExceptionDefine.NOT_IN_GAME.build();
        }

        return gameInstance.play(player, op);
    }
}
