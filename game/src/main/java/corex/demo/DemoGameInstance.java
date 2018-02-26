package corex.demo;

import corex.core.FutureMo;
import corex.core.define.TopicDefine;
import corex.core.utils.CoreXUtil;
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
        FutureMo b = FutureMo.futureMo();
        b.putString("gid", id);
        b.putBoolean("start", true);
        b.putInt("curP", player(curPlayer).seat());
        game.addBroadcast(CoreXUtil.externalBroadcast(room.roomChannel(), room.playerList(null), TopicDefine.GAME_INFO, b.toBodyHolder()));

    }

    @Override
    public void onPhase(int phase) {
        if (phase == size) {
            end();
            return;
        }

        ++curPlayer;

        logger.debug("onPhase, {}", phase);

        FutureMo b = FutureMo.futureMo();
        b.putString("gid", id);
        b.putInt("curP", player(curPlayer).seat());
        game.addBroadcast(CoreXUtil.externalBroadcast(room.roomChannel(), room.playerList(null), TopicDefine.GAME_INFO, b.toBodyHolder()));

        gotoPhase(phase + 1, 5000);
    }

    @Override
    public void onEnd() {

        logger.debug("onEnd");

        FutureMo b = FutureMo.futureMo();
        b.putString("gid", id);
        b.putBoolean("end", true);
        game.addBroadcast(CoreXUtil.externalBroadcast(room.roomChannel(), room.playerList(null), TopicDefine.GAME_INFO, b.toBodyHolder()));
    }
}
