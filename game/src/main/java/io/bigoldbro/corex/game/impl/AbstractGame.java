package io.bigoldbro.corex.game.impl;

import io.bigoldbro.corex.Context;
import io.bigoldbro.corex.ContextAware;
import io.bigoldbro.corex.annotation.Api;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.annotation.Notice;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.model.Auth;
import io.bigoldbro.corex.model.Broadcast;
import io.bigoldbro.corex.rpc.ModuleParams;
import io.bigoldbro.corex.rpc.RpcHandler;
import io.bigoldbro.corex.rpc.ServerModuleScanner;
import io.bigoldbro.corex.game.Game;
import io.bigoldbro.corex.game.GameInstance;
import io.bigoldbro.corex.game.Player;
import io.bigoldbro.corex.game.Xpusher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Created by Joshua on 2018/3/26.
 */
public abstract class AbstractGame implements Game, ContextAware, Xpusher {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ModuleParams moduleParams;

    private Context context;
    private Xpusher xpusher;
    private Map<String, Long> timeEvents = new HashMap<>();
    private Map<String, Player> players = new HashMap<>();
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
        onGameInit();
    }

    public final void destroy() {
        onGameDestroy();
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
    public Player getPlayer(String userId) {
        return players.get(userId);
    }

    public void setTimer(String type, long periodTime, Runnable task) {
        cancelTimeEvent(type);
        if (periodTime <= 0) {
            context().runOnContext(v -> runWrappedTask(task, Auth.internalAuth()));
        } else {
            long tid = coreX().setTimer(periodTime, t -> {
                timeEvents.remove(type);
                runWrappedTask(task, Auth.internalAuth());
            });
            timeEvents.put(type, tid);
        }

    }

    public void setPeriodic(String type, long delay, Runnable task) {
        cancelTimeEvent(type);
        long tid = coreX().setPeriodic(delay, t -> {
            runWrappedTask(task, Auth.internalAuth());
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
        Player player = ensurePlayer(userId);
        try {
            runWrappedTask(() -> player.setOnline(true), Auth.internalAuth());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void handlePlayerOffline(String userId) {
        Player player = ensurePlayer(userId);
        try {
            runWrappedTask(() -> player.setOnline(false), Auth.internalAuth());
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

    public void addPlayer(Player player) {
        players.put(player.userId(), player);
        logger.info("玩家 {} 加入游戏", player.nickName());
    }

    public void removePlayer(Player player) {
        players.remove(player.userId());
        logger.info("玩家 {} 离开游戏", player.nickName());
    }

    public boolean addGame(GameInstance gameInstance) {
        return instances.putIfAbsent(gameInstance.id(), gameInstance) == null;
    }

    public boolean removeGame(GameInstance gameInstance) {
        return instances.remove(gameInstance.id()) != null;
    }

    // 当前游戏人数是否已满
    protected final void checkLimit() {
        if (playerLimit() > 0 && playerNum() >= playerLimit()) {
            throw ExceptionDefine.GAME_PLAYER_LIMIT.build();
        }
    }

    protected Player ensurePlayer(String userId) {
        Player player = players.get(userId);
        if (player == null) {
            throw ExceptionDefine.NOT_IN_GAME.build();
        }
        return player;
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

    public boolean isPlayerJoined(String userId) {
        return getPlayer(userId) != null;
    }

    @Override
    public String userId() {
        return xpusher.userId();
    }

    @Override
    public void addBroadcast(Broadcast broadcast) {
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
        protected RpcHandler newBroadcastHandler(Notice notice, Method m, Object invoker) {
            return new WrappedRpcHandler(super.newBroadcastHandler(notice, m, invoker));
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
        public JoHolder handle(Auth auth, JsonObjectImpl params) throws Exception {
            return runWrappedTask(() -> rpcHandler.handle(auth, params), auth);
        }

        @Override
        public JsonObjectImpl convert(Object[] args) throws Exception {
            return rpcHandler.convert(args);
        }
    }
}
