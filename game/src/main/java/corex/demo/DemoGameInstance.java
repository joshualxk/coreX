package corex.demo;

import corex.core.define.TopicDefine;
import corex.core.json.JsonObject;
import corex.core.model.Broadcast;
import corex.core.utils.RandomUtil;
import corex.game.RoomPlayer;
import corex.game.impl.AbstractGame;
import corex.game.impl.AbstractGameInstance;

import java.util.List;

/**
 * Created by Joshua on 2018/3/28.
 */
public class DemoGameInstance extends AbstractGameInstance {

    private int curPlayer;

    public DemoGameInstance(AbstractGame game, DemoGameRoom room, String id, List<RoomPlayer> players) {
        super(game, room, id, players, 2);
    }

    @Override
    public void onStart() {
        curPlayer = RandomUtil.nextInt(size);

        logger.debug("onStart");

        gotoPhase(1, 5000);

        JsonObject jo = new JsonObject();
        jo.put("gid", id);
        jo.put("start", true);
        jo.put("curP", player(curPlayer).seat());
        game.addBroadcast(Broadcast.newExternalBroadcast(room.roomChannel(), room.playerList(null), TopicDefine.GAME_INFO, jo));

    }

    @Override
    public void onPhase(int phase) {
        if (phase == size) {
            end();
            return;
        }

        ++curPlayer;

        logger.debug("onPhase, {}", phase);

        JsonObject jo = new JsonObject();
        jo.put("gid", id);
        jo.put("curP", player(curPlayer).seat());
        game.addBroadcast(Broadcast.newExternalBroadcast(room.roomChannel(), room.playerList(null), TopicDefine.GAME_INFO, jo));

        gotoPhase(phase + 1, 5000);
    }

    @Override
    public void onEnd() {

        logger.debug("onEnd");

        JsonObject jo = new JsonObject();
        jo.put("gid", id);
        jo.put("end", true);
        game.addBroadcast(Broadcast.newExternalBroadcast(room.roomChannel(), room.playerList(null), TopicDefine.GAME_INFO, jo));
    }
}
