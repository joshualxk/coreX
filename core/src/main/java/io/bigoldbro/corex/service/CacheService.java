package io.bigoldbro.corex.service;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.define.CacheDefine;
import io.bigoldbro.corex.impl.GameRoute;
import io.bigoldbro.corex.model.ServerInfo;
import io.bigoldbro.corex.module.CacheModule;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Created by Joshua on 2018/4/4.
 */
public class CacheService extends SimpleModuleService implements CacheModule {

    private static final long UPDATE_CACHE_INTERVAL = 5 * 60 * 1000; // 5min;

    private final Map<String, Object> caches = new ConcurrentHashMap<>();

    @Override
    public void start(Future<Void> completeFuture) {
        completeFuture.complete();
    }

    private void updateCache0() {
        updateServerInfo();
        updateGameRoute();

        coreX().broadcast().onServerInfoUpdate(System.currentTimeMillis());
    }

    private void updateServerInfo() {
        List<ServerInfo> list = null;//basicDao.selectServerInfos();

        caches.put(CacheDefine.SERVER_INFO, list);
    }

    private void updateGameRoute() {
        List<GameRoute> list = null;//basicDao.selectGameRoutes();

        caches.put(CacheDefine.ROUTE_INFO, list);
    }

    @SuppressWarnings("unchecked")
    public static List<ServerInfo> parseServerInfos(Object object, Predicate<ServerInfo> predicate) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<GameRoute> parseGameRoutes(Object object) {
        return null;
    }

    @Override
    public void updateCache() {
    }

    @Override
    public void getCache(String name) {
    }

}
