package corex.game.impl;

import corex.core.define.ExceptionDefine;
import corex.core.define.TopicDefine;
import corex.core.json.JsonObject;
import corex.core.model.Broadcast;
import corex.game.GameInstance;
import corex.game.Room;
import corex.game.RoomPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joshua on 2018/3/26.
 */
public abstract class AbstractRoom implements Room {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final AbstractGame game;
    protected final int id;
    protected final String roomChannel;
    protected final int limit;
    protected final RoomPlayer[] roomPlayers;

    private GameInstance gameInstance;
    private long startAt;
    private int num;

    public AbstractRoom(AbstractGame game, int id, int limit) {
        this.game = game;
        this.id = id;
        this.roomChannel = createRoomChannel();
        this.limit = limit;
        roomPlayers = new RoomPlayer[limit];
    }

    @Override
    public int id() {
        return id;
    }

    public boolean isFull() {
        return num >= limit;
    }

    @Override
    public boolean hasGameBegun() {
        return gameInstance != null;
    }

    public GameInstance getGameInstance() {
        return gameInstance;
    }

    public void setGameInstance(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public int num() {
        return num;
    }

    @Override
    public String roomChannel() {
        return roomChannel;
    }

    protected AbstractGame game() {
        return game;
    }

    protected abstract String createRoomChannel();

    @Override
    public void addPlayer(RoomPlayer player) {
        if (hasGameBegun()) {
            throw ExceptionDefine.GAME_HAS_BEGUN.build();
        }

        int seat = findEmptySeat();
        if (seat < 0) {
            throw ExceptionDefine.ROOM_NUM_LIMIT.build();
        }
        addPlayer(seat, player);
        onPlayerEnter(player);
    }

    @Override
    public void removePlayer(RoomPlayer player) {
        if (hasGameBegun()) {
            throw ExceptionDefine.GAME_HAS_BEGUN.build();
        }

        int seat = player.seat();
        if (seat < 0) {
            throw ExceptionDefine.NOT_IN_ROOM.build();
        }
        removePlayer(seat, player);
        onPlayerLeave(player);
    }

    private boolean addPlayer(int index, RoomPlayer roomPlayer) {
        game.addPlayer(roomPlayer);
        roomPlayers[index] = roomPlayer;
        roomPlayer.setSeat(index);
        roomPlayer.setRoom(this);
        num++;
        return true;
    }

    private boolean removePlayer(int index, RoomPlayer roomPlayer) {
        game.removePlayer(roomPlayers[index]);
        roomPlayers[index] = null;
        roomPlayer.setSeat(-1);
        roomPlayer.setRoom(null);
        num--;
        return true;
    }

    /**
     * 第一个空位置
     *
     * @return
     */
    private int findEmptySeat() {
        for (int i = 0; i < limit; ++i) {
            if (roomPlayers[i] == null) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onPlayerEnter(RoomPlayer roomPlayer) {
        logger.debug("onPlayerEnter.");

        JsonObject jo = new JsonObject();
        jo.put("roomPlayer", roomPlayer.toJo());
        jo.put("enter", true);
        game().addBroadcast(Broadcast.newExternalBroadcast(roomChannel(), playerList(roomPlayer.userId()), TopicDefine.GAME_INFO, jo));
    }

    @Override
    public void onPlayerLeave(RoomPlayer roomPlayer) {
        logger.debug("onPlayerLeave.");

        JsonObject jo = new JsonObject();
        jo.put("seat", roomPlayer.seat());
        jo.put("leave", true);
        game().addBroadcast(Broadcast.newExternalBroadcast(roomChannel(), playerList(roomPlayer.userId()), TopicDefine.GAME_INFO, jo));
    }

    @Override
    public void onPlayerPrepared(RoomPlayer roomPlayer) {
        logger.debug("onPlayerPrepared.");

        JsonObject jo = new JsonObject();
        jo.put("seat", roomPlayer.seat());
        jo.put("prepare", true);
        game().addBroadcast(Broadcast.newExternalBroadcast(roomChannel(), playerList(roomPlayer.userId()), TopicDefine.GAME_INFO, jo));
    }

    @Override
    public void onPlayerCancelPrepared(RoomPlayer roomPlayer) {
        logger.debug("onPlayerCancelPrepared.");

        JsonObject jo = new JsonObject();
        jo.put("seat", roomPlayer.seat());
        jo.put("prepare", false);
        game().addBroadcast(Broadcast.newExternalBroadcast(roomChannel(), playerList(roomPlayer.userId()), TopicDefine.GAME_INFO, jo));
    }

    @Override
    public void onPlayerOnline(RoomPlayer roomPlayer) {
        logger.debug("onPlayerOnline.");
    }

    @Override
    public void onPlayerOffline(RoomPlayer roomPlayer) {
        logger.debug("onPlayerOffline.");
    }

    @Override
    public void onPlayerPresent(RoomPlayer roomPlayer) {
        logger.debug("onPlayerPresent.");

        JsonObject jo = new JsonObject();
        jo.put("roomPlayer", roomPlayer.nickName());
        jo.put("online", true);
        game().addBroadcast(Broadcast.newExternalBroadcast(roomChannel(), playerList(roomPlayer.userId()), TopicDefine.GAME_INFO, jo));
    }

    @Override
    public void onPlayerAbsent(RoomPlayer roomPlayer) {
        logger.debug("onPlayerAbsent.");

        JsonObject jo = new JsonObject();
        jo.put("roomPlayer", roomPlayer.nickName());
        jo.put("online", false);
        game().addBroadcast(Broadcast.newExternalBroadcast(roomChannel(), playerList(roomPlayer.userId()), TopicDefine.GAME_INFO, jo));
    }

    protected void broadcastRoomInfo() {
        JsonObject jo = new JsonObject();
        jo.put("room", toJo());
        game().addBroadcast(Broadcast.newExternalBroadcast(roomChannel(), playerList(null), TopicDefine.GAME_INFO, jo));
    }

    @Override
    public void reset() {
        if (gameInstance != null) {
            game().removeGame(gameInstance);
            setGameInstance(null);
        }

        for (RoomPlayer player : roomPlayers) {
            if (player != null) {
                player.setPrepared(false, false);
                player.setGameInstance(null);
            }
        }
        setStartAt(0);
        broadcastRoomInfo();
    }

    public List<String> playerList(String excepted) {
        List<String> list = new ArrayList<>(num);
        for (RoomPlayer p : roomPlayers) {
            if (p != null && !p.userId().equals(excepted)) {
                list.add(p.userId());
            }
        }
        return list;
    }

    protected void setStartAt(long startAt) {
        this.startAt = startAt;
    }

    public long getStartAt() {
        return startAt;
    }
}
