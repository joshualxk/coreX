package corex.demo;

import corex.core.JoHolder;
import corex.core.define.ExceptionDefine;
import corex.core.json.JsonObject;
import corex.game.Room;
import corex.game.RoomPlayer;
import corex.game.impl.AbstractGame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Joshua on 2018/3/26.
 */
public class DemoGameImpl extends AbstractGame implements DemoGame {

    private final Map<Integer, Set<Integer>> typeRoomsMaps = new HashMap<>();

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
    public void onInit() {
        for (int i = 0; i < 4; ++i) {
            addTTTRoom(new DemoGameRoom(this, i + 1, 3, i % 2));
        }
    }

    @Override
    public void onDestroy() {
    }

    private boolean addTTTRoom(DemoGameRoom room) {
        if (addRoom(room)) {
            return typeRoomsMaps.computeIfAbsent(room.type(), key -> new HashSet<>()).add(room.id());
        }
        return false;
    }

    private boolean removeTTTRoom(DemoGameRoom room) {
        if (removeRoom(room)) {
            return typeRoomsMaps.computeIfAbsent(room.type(), key -> new HashSet<>()).remove(room.id());
        }
        return false;
    }

    @Override
    public JoHolder connect() {
        JoHolder ret = JoHolder.newSync();
        ret.jo().put("code", 123);
        return ret;
    }

    @Override
    public JoHolder match(int type) {
        String userId = userId();

        JoHolder ret = JoHolder.newSync();
        JsonObject jo = ret.jo();

        if (isPlayerJoined(userId)) {
            jo.put("info", "你已在游戏中");
        } else {
            checkLimit();
            // 匹配

            if (!typeRoomsMaps.containsKey(type)) {
                throw ExceptionDefine.PARAM_ERR.build();
            }

            Room found = null;
            for (int roomId : typeRoomsMaps.get(type)) {
                Room room = getRoom(roomId);
                if (!room.isFull() && !room.hasGameBegun()) {
                    found = room;
                    break;
                }
            }

            if (found == null) {
                throw ExceptionDefine.ROOM_NOT_MATCH.build();
            }

            RoomPlayer roomPlayer = new DemoGamePlayer(userId, "roomPlayer" + userId, null, false, true, true, false);
            roomPlayer.enterRoom(found);

            jo.put("roomId", found.id());
            jo.put("u", roomPlayer.toJo());
        }

        return ret;
    }

    @Override
    public JoHolder leave() {

        RoomPlayer player = ensurePlayer(userId());
        player.leaveRoom();

        return JoHolder.newSync();
    }

    @Override
    public JoHolder prepare(boolean prepared) {

        RoomPlayer player = ensurePlayer(userId());
        if (prepared) {
            player.prepare();
        } else {
            player.cancelPrepare();
        }

        return JoHolder.newSync();
    }

    @Override
    public JoHolder play(int sjb) {
        RoomPlayer player = ensurePlayer(userId());

        return JoHolder.newSync();
    }
}
