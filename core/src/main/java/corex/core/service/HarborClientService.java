package corex.core.service;

import corex.core.Future;
import corex.core.JoHolder;
import corex.core.Msg;
import corex.core.define.CacheDefine;
import corex.core.define.ConstDefine;
import corex.core.define.ExceptionDefine;
import corex.core.define.ServiceNameDefine;
import corex.core.exception.BizException;
import corex.core.impl.BroadcastReceiver;
import corex.core.impl.GameRoute;
import corex.core.impl.MsgPostman;
import corex.core.impl.ServerInfo;
import corex.core.model.Broadcast;
import corex.core.model.Method;
import corex.core.model.Payload;
import corex.core.model.RpcRequest;
import corex.module.CacheModule;
import corex.module.HarborClientModule;

import java.util.List;

import static corex.core.utils.CoreXUtil.isRole;

/**
 * Created by Joshua on 2018/3/1.
 */
public class HarborClientService extends SimpleModuleService implements HarborClientModule {

    private BroadcastReceiver broadcastReceiver;
    private MsgPostman msgPostman;

    public HarborClientService() {
        this.broadcastReceiver = new InternalBroadcastReceiver();
    }

    @Override
    public void start(Future<Void> completeFuture) {
        msgPostman = new MsgPostman(context);
        msgPostman.msgHandler(this::handleMsg);

        this.broadcastReceiver.init(context);

        completeFuture.complete();
    }

    private boolean checkAddConnection(ServerInfo serverInfo) {
        if (serverInfo.getServerId() == coreX().serverId()) {
            return false;
        }
        if (isRole(coreX().role(), ConstDefine.ROLE_GATEWAY)) {
            return true;
        } else {
            return isRole(serverInfo.getRole(), ConstDefine.ROLE_BROADCAST);
        }
    }

    @Override
    protected void handleBroadcast(Msg msg, Payload payload, Broadcast broadcast) {
        coreX().onBroadcast(payload.getBroadcast());
        if (broadcast.getRole() == ConstDefine.ROLE_LOCAL) {
            return;
        }
        try {
            msgPostman.writeToRandomRole(ConstDefine.ROLE_BROADCAST, payload);
        } catch (Exception ignore) {

        }
    }

    @Override
    protected void handleRequest(Msg msg, Payload payload, RpcRequest request) {
        Method method = request.getMethod();
        String module = method.getModule();
        try {
            if (name().equals(module)) {
                super.handleRequest(msg, payload, request);
            } else if (ServiceNameDefine.LOGIN.equals(module)) {
                msgPostman.writeToRandomRole(ConstDefine.ROLE_AUTH, payload);
            } else {
                msgPostman.deliver(module, method.getVersion(), payload);
            }
            return;
        } catch (BizException e) {

        } catch (Exception e) {
            logger.warn("转发 RpcRequest 失败.", e);
        }
        msg.reply(Future.failedFuture(ExceptionDefine.NOT_FOUND.build()));
    }

    private void handleMsg(Object msg) {
        if (msg instanceof Payload) {
            Payload payload = (Payload) msg;

            if (payload.hasRpcResponse()) {
                coreX().onMsgReply(payload.getId(), Future.succeededFuture(payload));
            } else if (payload.hasBroadcast()) {
                coreX().onBroadcast(payload.getBroadcast());
            }
        }
    }

    @Override
    public JoHolder info() {
        JoHolder ret = super.info();
        ret.jo().put("conns", msgPostman.info());
        return ret;
    }

    private class InternalBroadcastReceiver extends BroadcastReceiver {

        public InternalBroadcastReceiver() {
            super(true, false);
        }

        @Override
        public void onServerInfoUpdate(long updateTime) {
            logger.debug("---------------------------> #onServerInfoUpdate# at {}.", updateTime);

            coreX().asyncAgent(CacheModule.class).getCache(CacheDefine.SERVER_INFO).addListener(ar -> {
                if (ar.succeeded()) {

                    List<ServerInfo> list = CacheService.parseServerInfos(ar.result().jo(), HarborClientService.this::checkAddConnection);
                    msgPostman.updateServerInfos(list);

                    logger.debug("---------------------------> #onServerInfoUpdate# {} at {} success.", CacheDefine.SERVER_INFO, updateTime);
                } else {
                    logger.debug("---------------------------> #onServerInfoUpdate# {} at {} failed.", CacheDefine.SERVER_INFO, updateTime, ar.cause());
                }
            });

            coreX().asyncAgent(CacheModule.class).getCache(CacheDefine.ROUTE_INFO).addListener(ar -> {
                if (ar.succeeded()) {
                    List<GameRoute> list = CacheService.parseGameRoutes(ar.result().jo());
                    msgPostman.updateRoutes(list);

                    logger.debug("---------------------------> #onServerInfoUpdate# {} at {} success.", CacheDefine.ROUTE_INFO, updateTime);
                } else {
                    logger.debug("---------------------------> #onServerInfoUpdate# {} at {} failed.", CacheDefine.ROUTE_INFO, updateTime, ar.cause());
                }
            });
        }
    }
}
