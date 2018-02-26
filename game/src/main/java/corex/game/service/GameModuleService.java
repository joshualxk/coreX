package corex.game.service;

import corex.core.Future;
import corex.core.annotation.Module;
import corex.core.impl.BroadcastReceiver;
import corex.core.rpc.RpcHandler;
import corex.core.service.AbstractProcessorService;
import corex.game.impl.AbstractGame;

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
