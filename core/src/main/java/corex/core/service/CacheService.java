package corex.core.service;

import corex.core.Future;
import corex.core.JoHolder;
import corex.core.define.CacheDefine;
import corex.core.impl.GameRoute;
import corex.core.impl.ServerInfo;
import corex.core.json.JsonArray;
import corex.core.json.JsonObject;
import corex.dao.BasicDao;
import corex.module.CacheModule;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    BasicDao basicDao;

    private final Map<String, JsonObject> caches = new ConcurrentHashMap<>();

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
        List<ServerInfo> list = basicDao.selectServerInfos();

        JsonArray ja = new JsonArray();
        for (ServerInfo si : list) {
            ja.add(si.toJo());
        }

        long updateTime = System.currentTimeMillis();
        JsonObject cache = new JsonObject();

        cache.put("name", CacheDefine.SERVER_INFO);
        cache.put("updateTime", updateTime);
        cache.put("body", ja);

        caches.put(CacheDefine.SERVER_INFO, cache);
    }

    private void updateGameRoute() {
        List<GameRoute> list = basicDao.selectGameRoutes();

        JsonArray ja = new JsonArray();
        for (GameRoute si : list) {
            ja.add(si.toJo());
        }

        long updateTime = System.currentTimeMillis();
        JsonObject cache = new JsonObject();

        cache.put("name", CacheDefine.ROUTE_INFO);
        cache.put("updateTime", updateTime);
        cache.put("body", ja);

        caches.put(CacheDefine.ROUTE_INFO, cache);
    }

    @SuppressWarnings("unchecked")
    public static List<ServerInfo> parseServerInfos(JsonObject jo, Predicate<ServerInfo> predicate) {
        JsonArray ja = jo.getJsonObject("cache").getJsonArray("body");
        return ((List<JsonObject>) ja.getList()).stream().map(ServerInfo::fromJo)
                .filter(predicate).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static List<GameRoute> parseGameRoutes(JsonObject jo) {
        JsonArray ja = jo.getJsonObject("cache").getJsonArray("body");
        return ((List<JsonObject>) ja.getList()).stream().map(GameRoute::fromJo).collect(Collectors.toList());
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
