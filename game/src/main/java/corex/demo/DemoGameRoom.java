package corex.demo;

import corex.core.FutureMo;
import corex.core.Lo;
import corex.core.Mo;
import corex.core.define.TopicDefine;
import corex.core.utils.CoreXUtil;
import corex.game.RoomPlayer;
import corex.game.impl.AbstractRoom;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joshua on 2018/3/27.
 */
public class DemoGameRoom extends AbstractRoom {

    private final int type;

    private boolean hasTriggered;

    public DemoGameRoom(DemoGameImpl game, int id, int limit, int type) {
        super(game, id, limit);
        this.type = type;
    }

    public int type() {
        return type;
    }

    @Override
    protected String createRoomChannel() {
        return String.format("outbound-%04d-1", id());
    }

    @Override
    public void onPlayerEnter(RoomPlayer roomPlayer) {
        super.onPlayerEnter(roomPlayer);
    }

    @Override
    public void onPlayerLeave(RoomPlayer roomPlayer) {
        super.onPlayerLeave(roomPlayer);
        checkTriggerStartCondition();
    }

    @Override
    public void onPlayerPrepared(RoomPlayer roomPlayer) {
        super.onPlayerPrepared(roomPlayer);
        checkTriggerStartCondition();
    }

    @Override
    public void onPlayerCancelPrepared(RoomPlayer roomPlayer) {
        super.onPlayerCancelPrepared(roomPlayer);
        checkTriggerStartCondition();
    }

    @Override
    public void onPlayerOnline(RoomPlayer roomPlayer) {
        super.onPlayerOnline(roomPlayer);
    }

    @Override
    public void onPlayerOffline(RoomPlayer roomPlayer) {
        super.onPlayerOffline(roomPlayer);
    }

    @Override
    public void onPlayerPresent(RoomPlayer roomPlayer) {
        super.onPlayerPresent(roomPlayer);
    }

    @Override
    public void onPlayerAbsent(RoomPlayer roomPlayer) {
        super.onPlayerAbsent(roomPlayer);
    }

    private void checkTriggerStartCondition() {
        int preparedNum = 0;
        for (RoomPlayer player : roomPlayers) {
            if (player != null && player.isPrepared()) {
                preparedNum++;
            }
        }
        updateTrigger(preparedNum >= 2);
    }

    private boolean updateTrigger(boolean trigger) {
        if (this.hasTriggered != trigger) {
            this.hasTriggered = trigger;
            if (trigger) {
                startTimeEvent();
            } else {
                cancelStartTimeEvent();
            }
            return true;
        }
        return false;
    }

    private static String startEventType(int roomId) {
        return "start_ev_" + roomId;
    }

    private void startTimeEvent() {
        long delay = 6000;
        setStartAt(System.currentTimeMillis() + delay);
        game().setTimeEvent(startEventType(id()), delay, this::startGame);

        FutureMo b = FutureMo.futureMo();
        b.putLong("startAt", getStartAt());
        game().addBroadcast(CoreXUtil.externalBroadcast(roomChannel(), playerList(null), TopicDefine.GAME_INFO, b.toBodyHolder()));
    }

    private void cancelStartTimeEvent() {
        game().cancelTimeEvent(startEventType(id()));
        setStartAt(0);

        FutureMo b = FutureMo.futureMo();
        b.putLong("startAt", getStartAt());
        game().addBroadcast(CoreXUtil.externalBroadcast(roomChannel(), playerList(null), TopicDefine.GAME_INFO, b.toBodyHolder()));
    }

    private void startGame() {
        try {
            String id = game().gameId() + "_" + id() + "_" + System.currentTimeMillis();
            List<RoomPlayer> players = new ArrayList<>(limit);
            for (RoomPlayer player : this.roomPlayers) {
                if (player != null && player.isPrepared()) {
                    players.add(player);
                }
            }
            DemoGameInstance demoGameInstance = new DemoGameInstance(game(), this, id, players);
            demoGameInstance.start();
        } catch (Exception e) {
            logger.warn("开始游戏失败.", e);
            reset();
        }
    }

    @Override
    public Mo toMo() {
        Mo mo = Mo.mo();
        mo.putInt("id", id());
        mo.putInt("t", type());
        mo.putInt("n", num());
        mo.putLong("sa", getStartAt());

        if (num() > 0) {
            Lo lo = Lo.lo();
            for (RoomPlayer roomPlayer : this.roomPlayers) {
                if (roomPlayer != null) {
                    lo.addMo(roomPlayer.toMo());
                }
            }
            mo.putList("pl", lo);
        }

        return mo;
    }
}
