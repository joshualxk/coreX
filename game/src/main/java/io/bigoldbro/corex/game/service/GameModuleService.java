package io.bigoldbro.corex.game.service;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.impl.BroadcastReceiver;
import io.bigoldbro.corex.rpc.RpcHandler;
import io.bigoldbro.corex.service.AbstractProcessorService;
import io.bigoldbro.corex.game.impl.AbstractGame;

/**
 * Created by Joshua on 2018/3/23.
 */
public class GameModuleService extends AbstractProcessorService {

    private final BroadcastReceiver broadcastReceiver;
    private final AbstractGame abstractGame;

    public GameModuleService(AbstractGame abstractGame) {
        this.abstractGame = abstractGame;
        this.broadcastReceiver = new InternalBroadcastReceiver();
    }

    @Override
    public void start(Future<Void> completeFuture) {
        abstractGame.init(context());
        broadcastReceiver.init(context());

        completeFuture.complete();
    }

    @Override
    public void stop(Future<Void> completeFuture) {
        abstractGame.destroy();
        completeFuture.complete();
    }

    @Override
    protected RpcHandler getHandler(String name) {
        return abstractGame.getHandler(name);
    }

    @Override
    protected Module getModule() {
        return abstractGame.module();
    }

    private class InternalBroadcastReceiver extends BroadcastReceiver {

        public InternalBroadcastReceiver() {
            super(true, false);
        }

        @Override
        public void onUserLogin(String userId, int serverId, long loginTime) {
            if (abstractGame.isPlayerJoined(userId)) {
                abstractGame.handlePlayerOnline(userId);
            }
        }

        @Override
        public void onUserLogout(String userId, int serverId, long loginTime) {
            if (abstractGame.isPlayerJoined(userId)) {
                abstractGame.handlePlayerOffline(userId);
            }
        }
    }
}
