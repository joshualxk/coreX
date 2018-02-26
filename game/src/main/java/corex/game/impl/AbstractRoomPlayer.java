package corex.game.impl;

import corex.core.define.ExceptionDefine;
import corex.game.GameInstance;
import corex.game.Room;
import corex.game.RoomPlayer;

/**
 * Created by Joshua on 2018/3/26.
 */
public abstract class AbstractRoomPlayer implements RoomPlayer {

    private final String userId;
    private final String nickName;
    private final String icon;
    private final boolean isRobot;

    private Room room;
    private GameInstance gameInstance;
    private int seat = -1;
    private boolean online;
    private boolean present;
    private boolean prepared;

    public AbstractRoomPlayer(String userId, String nickName, String icon, boolean isRobot, boolean online, boolean present, boolean prepared) {
        this.userId = userId;
        this.nickName = nickName;
        this.icon = icon;
        this.isRobot = isRobot;
        this.online = online;
        this.present = present;
        this.prepared = prepared;
    }

    @Override
    public String userId() {
        return userId;
    }

    @Override
    public String nickName() {
        return nickName;
    }

    @Override
    public String icon() {
        return icon;
    }

    @Override
    public boolean isRobot() {
        return isRobot;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public boolean setOnline(boolean online) {
        if (this.online != online) {
            this.online = online;
            if (online) {
                onOnline();
            } else {
                onOffline();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isPresent() {
        return present;
    }

    @Override
    public boolean setPresent(boolean present) {
        if (this.present != present) {
            this.present = present;
            if (present) {
                onPresent();
            } else {
                onAbsent();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isPrepared() {
        return prepared;
    }

    @Override
    public boolean setPrepared(boolean prepared, boolean propagate) {
        if (this.prepared != prepared) {
            this.prepared = prepared;

            if (propagate) {
                if (prepared) {
                    onPrepared();
                } else {
                    onCancelPrepared();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int seat() {
        return seat;
    }

    @Override
    public void setSeat(int seat) {
        this.seat = seat;
    }

    @Override
    public Room room() {
        return room;
    }

    @Override
    public GameInstance gameInstance() {
        return gameInstance;
    }

    @Override
    public void setGameInstance(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @Override
    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public void enterRoom(Room room) {
        if (this.room != null) {
            throw ExceptionDefine.IN_ROOM.build();
        }
        room.addPlayer(this);
    }

    @Override
    public void leaveRoom() {
        if (room == null) {
            throw ExceptionDefine.NOT_IN_ROOM.build();
        }
        room.removePlayer(this);
    }

    @Override
    public void prepare() {
        if (room == null) {
            throw ExceptionDefine.NOT_IN_ROOM.build();
        }
        if (room.hasGameBegun()) {
            throw ExceptionDefine.GAME_HAS_BEGUN.build();
        }
        setPrepared(true, true);
    }

    @Override
    public void cancelPrepare() {
        if (room == null) {
            throw ExceptionDefine.NOT_IN_ROOM.build();
        }
        room.onPlayerCancelPrepared(this);
    }

    @Override
    public void onOnline() {
        if (room() != null) {
            room().onPlayerOnline(this);
        }
    }

    @Override
    public void onOffline() {
        if (room() != null) {
            if (room.hasGameBegun()) {
                setPresent(false);
            } else {
                room().removePlayer(this);
            }
        }
    }

    @Override
    public void onPresent() {
        if (room() != null) {
            room().onPlayerPresent(this);
        }
    }

    @Override
    public void onAbsent() {
        if (room() != null) {
            room().onPlayerAbsent(this);
        }
    }

    @Override
    public void onPrepared() {
        if (room() != null) {
            room().onPlayerPrepared(this);
        }
    }

    @Override
    public void onCancelPrepared() {
        if (room() != null) {
            room().onPlayerCancelPrepared(this);
        }
    }
}
