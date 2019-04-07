package io.bigoldbro.corex.service;

import io.bigoldbro.corex.Callback;
import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.Msg;
import io.bigoldbro.corex.define.CacheDefine;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.define.ExceptionDefine;
import io.bigoldbro.corex.define.ServiceNameDefine;
import io.bigoldbro.corex.exception.BizException;
import io.bigoldbro.corex.impl.BroadcastReceiver;
import io.bigoldbro.corex.impl.GameRoute;
import io.bigoldbro.corex.impl.MsgPostman;
import io.bigoldbro.corex.impl.ServerInfo;
import io.bigoldbro.corex.json.JsonObject;
import io.bigoldbro.corex.model.Broadcast;
import io.bigoldbro.corex.model.Method;
import io.bigoldbro.corex.model.Payload;
import io.bigoldbro.corex.model.RpcRequest;
import io.bigoldbro.corex.module.CacheModule;
import io.bigoldbro.corex.module.HarborClientModule;

import java.util.List;

import static io.bigoldbro.corex.utils.CoreXUtil.isRole;

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
        } catch (BizException ignore) {
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
    public Callback<JsonObject> info() {
        Callback<JsonObject> ret = super.info();
        ret.result().put("conns", msgPostman.info());
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
