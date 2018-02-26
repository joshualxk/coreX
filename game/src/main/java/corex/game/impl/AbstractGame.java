package corex.game.impl;

import corex.core.Context;
import corex.core.ContextAware;
import corex.core.FutureMo;
import corex.core.Mo;
import corex.core.annotation.Api;
import corex.core.annotation.Broadcast;
import corex.core.annotation.Module;
import corex.core.define.ConstDefine;
import corex.core.define.ExceptionDefine;
import corex.core.rpc.ModuleParams;
import corex.core.rpc.RpcHandler;
import corex.core.rpc.ServerModuleScanner;
import corex.core.utils.CoreXUtil;
import corex.game.*;
import corex.proto.ModelProto;
import corex.proto.ModelProto.Auth;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Created by Joshua on 2018/3/26.
 */
public abstract class AbstractGame implements Game, ContextAware, Xpusher {

    private final ModuleParams moduleParams;

    private Context context;
    private XpusherImpl xpusher;
    private Map<String, Long> timeEvents = new HashMap<>();
    private Map<String, RoomPlayer> players = new HashMap<>();
    private Map<Integer, Room> rooms = new HashMap<>();
    private Map<String, GameInstance> instances = new HashMap<>();

    public AbstractGame(Class<?> clz) {
        ModuleParams moduleParams = new GameModuleScanner().parse(clz);
        this.moduleParams = moduleParams;
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public void init(Context context) {
        this.context = context;
        this.xpusher = new XpusherImpl(context);
        onInit();
    }


    public final void destroy() {
        onDestroy();
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public int playerNum() {
        return players.size();
    }

    @Override
    public int instanceNum() {
        return instances.size();
    }

    @Override
    public boolean isPlayerJoined(String userId) {
        return players.containsKey(userId);
    }

    public void setTimeEvent(String type, long periodTime, Runnable task) {
        cancelTimeEvent(type);
        long tid = coreX().setTimer(periodTime, t -> {
            timeEvents.remove(type);
            runWrappedTask(task, CoreXUtil.INTERNAL_AUTH);
        });
        timeEvents.put(type, tid);
    }

    public void cancelTimeEvent(String type) {
        Long tid = timeEvents.remove(type);
        if (tid != null) {
            coreX().cancelTimer(tid);
        }
    }

    public final void handlePlayerOnline(String userId) {
        RoomPlayer roomPlayer = ensurePlayer(userId);
        try {
            runWrappedTask(() -> roomPlayer.setOnline(true), CoreXUtil.INTERNAL_AUTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void handlePlayerOffline(String userId) {
        RoomPlayer roomPlayer = ensurePlayer(userId);
        try {
            runWrappedTask(() -> roomPlayer.setOnline(false), CoreXUtil.INTERNAL_AUTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final RpcHandler getHandler(String name) {
        return moduleParams.getHandler(name);
    }

    public final Module module() {
        return moduleParams.module();
    }

    public void addPlayer(RoomPlayer roomPlayer) {
        players.put(roomPlayer.userId(), roomPlayer);
    }

    public void removePlayer(RoomPlayer roomPlayer) {
        players.remove(roomPlayer.userId());
    }

    public boolean addGame(GameInstance gameInstance) {
        return instances.putIfAbsent(gameInstance.id(), gameInstance) == null;
    }

    public boolean removeGame(GameInstance gameInstance) {
        return instances.remove(gameInstance.id()) != null;
    }

    protected boolean addRoom(Room room) {
        return rooms.putIfAbsent(room.id(), room) == null;
    }

    protected boolean removeRoom(Room room) {
        return room.num() == 0 && rooms.remove(room.id()) != null;
    }

    protected Room getRoom(int roomId) {
        return rooms.get(roomId);
    }

    // 当前游戏人数是否已满
    protected final void checkLimit() {
        if (playerLimit() > 0 && playerNum() >= playerLimit()) {
            throw ExceptionDefine.GAME_PLAYER_LIMIT.build();
        }
    }

    protected RoomPlayer ensurePlayer(String userId) {
        RoomPlayer roomPlayer = players.get(userId);
        if (roomPlayer == null) {
            throw ExceptionDefine.NOT_IN_GAME.build();
        }
        return roomPlayer;
    }

    protected <V> V runWrappedTask(Callable<V> task, Auth auth) throws Exception {
        Callable<V> callable = () -> {
            before(auth);
            boolean success = false;
            try {
                V v = task.call();
                success = true;
                return v;
            } finally {
                after(success);
            }
        };
        return callable.call();
    }

    protected void runWrappedTask(Runnable task, Auth auth) {
        Runnable runnable = () -> {
            before(auth);
            boolean success = false;
            try {
                task.run();
                success = true;
            } finally {
                after(success);
            }
        };
        runnable.run();
    }

    @Override
    public void before(Auth auth) {
        if (isClosing() && auth.getType() == ConstDefine.AUTH_TYPE_CLIENT) {
            String userId = auth.getToken();
            if (!isPlayerJoined(userId)) {
                throw ExceptionDefine.GAME_CLOSING.build();
            }
        }
        xpusher.before(auth);
    }

    @Override
    public String userId() {
        return xpusher.userId();
    }

    @Override
    public void addBroadcast(ModelProto.Broadcast broadcast) {
        xpusher.addBroadcast(broadcast);
    }

    @Override
    public void after(boolean success) {
        xpusher.after(success);
    }

    private class GameModuleScanner extends ServerModuleScanner {

        public GameModuleScanner() {
            super(AbstractGame.this);
        }

        @Override
        protected RpcHandler newApiHandler(Api api, Method m, Object invoker) {
            return new WrappedRpcHandler(super.newApiHandler(api, m, invoker));
        }

        @Override
        protected RpcHandler newBroadcastHandler(Broadcast broadcast, Method m, Object invoker) {
            return new WrappedRpcHandler(super.newBroadcastHandler(broadcast, m, invoker));
        }
    }

    private class WrappedRpcHandler implements RpcHandler {

        private final RpcHandler rpcHandler;

        public WrappedRpcHandler(RpcHandler rpcHandler) {
            this.rpcHandler = Objects.requireNonNull(rpcHandler);
        }

        @Override
        public String name() {
            return rpcHandler.name();
        }

        @Override
        public boolean isVoidType() {
            return rpcHandler.isVoidType();
        }

        @Override
        public FutureMo handle(Auth auth, Mo params) throws Exception {
            return runWrappedTask(() -> rpcHandler.handle(auth, params), auth);
        }

        @Override
        public FutureMo convert(Object[] args) throws Exception {
            return rpcHandler.convert(args);
        }
    }
}
