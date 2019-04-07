package io.bigoldbro.corex.service;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.json.JsonArrayImpl;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.define.CacheDefine;
import io.bigoldbro.corex.impl.GameRoute;
import io.bigoldbro.corex.impl.ServerInfo;
import io.bigoldbro.corex.module.CacheModule;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Joshua on 2018/4/4.
 */
public class CacheService extends SimpleModuleService implements CacheModule {

    private static final long UPDATE_CACHE_INTERVAL = 5 * 60 * 1000; // 5min;

    private final Map<String, JsonObjectImpl> caches = new ConcurrentHashMap<>();

    @Override
    public void start(Future<Void> completeFuture) {
        completeFuture.complete();
    }

    @Override
    public void afterStart() {
        updateCache0();
        coreX().setPeriodic(UPDATE_CACHE_INTERVAL, tid -> updateCache());
    }

    private void updateCache0() {
        updateServerInfo();
        updateGameRoute();

        coreX().broadcast().onServerInfoUpdate(System.currentTimeMillis());
    }

    private void updateServerInfo() {
        List<ServerInfo> list = null;//basicDao.selectServerInfos();

        JsonArrayImpl ja = new JsonArrayImpl();
        for (ServerInfo si : list) {
            ja.add(si.toJo());
        }

        long updateTime = System.currentTimeMillis();
        JsonObjectImpl cache = new JsonObjectImpl();

        cache.put("name", CacheDefine.SERVER_INFO);
        cache.put("updateTime", updateTime);
        cache.put("body", ja);

        caches.put(CacheDefine.SERVER_INFO, cache);
    }

    private void updateGameRoute() {
        List<GameRoute> list = null;//basicDao.selectGameRoutes();

        JsonArrayImpl ja = new JsonArrayImpl();
        for (GameRoute si : list) {
            ja.add(si.toJo());
        }

        long updateTime = System.currentTimeMillis();
        JsonObjectImpl cache = new JsonObjectImpl();

        cache.put("name", CacheDefine.ROUTE_INFO);
        cache.put("updateTime", updateTime);
        cache.put("body", ja);

        caches.put(CacheDefine.ROUTE_INFO, cache);
    }

    @SuppressWarnings("unchecked")
    public static List<ServerInfo> parseServerInfos(JsonObjectImpl jo, Predicate<ServerInfo> predicate) {
        JsonArrayImpl ja = jo.getJsonObject("cache").getJsonArray("body");
        return ((List<JsonObjectImpl>) ja.getList()).stream().map(ServerInfo::fromJo)
                .filter(predicate).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static List<GameRoute> parseGameRoutes(JsonObjectImpl jo) {
        JsonArrayImpl ja = jo.getJsonObject("cache").getJsonArray("body");
        return ((List<JsonObjectImpl>) ja.getList()).stream().map(GameRoute::fromJo).collect(Collectors.toList());
    }

    @Override
    public JoHolder updateCache() {
        updateCache0();
        return JoHolder.newSync();
    }

    @Override
    public JoHolder getCache(String name) {
        JoHolder ret = JoHolder.newSync();
        ret.jo().put("cache", caches.get(name));
        return ret;
    }

}
