package corex.core.service;

import corex.core.Future;
import corex.core.FutureMo;
import corex.core.Lo;
import corex.core.Mo;
import corex.core.define.CacheDefine;
import corex.core.impl.GameRoute;
import corex.core.impl.ServerInfo;
import corex.dao.BasicDao;
import corex.module.CacheModule;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Joshua on 2018/4/4.
 */
public class CacheService extends SimpleModuleService implements CacheModule {

    private static final long UPDATE_CACHE_INTERVAL = 60 * 1000; // 60s;

    @Autowired
    BasicDao basicDao;

    private final Map<String, Mo> caches = new ConcurrentHashMap<>();

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

        Lo lo = Lo.lo();
        for (ServerInfo si : list) {
            lo.addMo(si.toMo());
        }

        long updateTime = System.currentTimeMillis();
        Mo cache = Mo.mo();

        cache.putString("name", CacheDefine.SERVER_INFO);
        cache.putLong("updateTime", updateTime);
        cache.putList("body", lo);

        caches.put(CacheDefine.SERVER_INFO, cache);
    }

    private void updateGameRoute() {
        List<GameRoute> list = basicDao.selectGameRoutes();

        Lo lo = Lo.lo();
        for (GameRoute si : list) {
            lo.addMo(si.toMo());
        }

        long updateTime = System.currentTimeMillis();
        Mo cache = Mo.mo();

        cache.putString("name", CacheDefine.ROUTE_INFO);
        cache.putLong("updateTime", updateTime);
        cache.putList("body", lo);

        caches.put(CacheDefine.ROUTE_INFO, cache);
    }

    @Override
    public FutureMo info() {
        return baseInfo();
    }

    @Override
    public FutureMo updateCache() {
        updateCache0();
        FutureMo futureMo = FutureMo.futureMo();
        return futureMo;
    }

    @Override
    public FutureMo getCache(String name) {
        FutureMo futureMo = FutureMo.futureMo();
        futureMo.putMo("cache", caches.get(name));
        return futureMo;
    }

}
