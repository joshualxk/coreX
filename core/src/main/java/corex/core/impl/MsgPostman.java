package corex.core.impl;

import corex.core.Context;
import corex.core.Handler;
import corex.core.define.ConstDefine;
import corex.core.define.ExceptionDefine;
import corex.core.json.JsonObject;
import corex.core.utils.CoreXUtil;
import corex.core.utils.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static corex.core.impl.ServerInfo.NON_SERVER_ID;

/**
 * Created by Joshua on 2018/3/20.
 */
public class MsgPostman {

    protected static final Logger logger = LoggerFactory.getLogger(MsgPostman.class);

    private final RecoverableConnectionManager recoverableConnectionManager;
    private final ConnectionCache cache = new ConnectionCache();

    private final Map<Integer, ServerInfo> allServerInfos = new HashMap<>(32);

    private Handler<Object> msgHandler;

    public MsgPostman(Context context) {
        this.recoverableConnectionManager = new RecoverableConnectionManager(context, true);
        this.recoverableConnectionManager.openHandler(conn -> {
            RecoverableConnection recoverableConnection = (RecoverableConnection) conn;

            final int serverId = recoverableConnection.serverId();
            final int role = recoverableConnection.role();

            cache.add(role, serverId);

            recoverableConnection.recoverHandler(v -> {
                cache.add(role, serverId);
            });

            recoverableConnection.msgHandler(msgHandler);

            recoverableConnection.errorHandler(v -> {
                cache.remove(role, serverId);
            });

            recoverableConnection.closeHandler(v -> {
                cache.remove(role, serverId);
            });
        });
    }

    public void updateServerInfos(List<ServerInfo> serverInfos) {
        List<ServerInfo> addList = new ArrayList<>(serverInfos.size());
        List<ServerInfo> keepList = new ArrayList<>(serverInfos.size());
        List<ServerInfo> delList = new ArrayList<>(allServerInfos.size());

        for (ServerInfo si : serverInfos) {
            ServerInfo oldV = allServerInfos.remove(si.getServerId());
            if (oldV == null) {
                addList.add(si);
            } else if (oldV.equals(si)) {
                keepList.add(si);
            } else {
                delList.add(oldV);
                addList.add(si);
            }
        }

        delList.addAll(allServerInfos.values());
        allServerInfos.clear();

        logger.info("#updateServerInfos# +{}, -{}, ={}.", addList.size(), delList.size(), keepList.size());

        for (ServerInfo si : delList) {
            recoverableConnectionManager.removeConnection(si);
        }
        for (ServerInfo si : keepList) {
            allServerInfos.put(si.getServerId(), si);
        }
        for (ServerInfo si : addList) {
            allServerInfos.put(si.getServerId(), si);
            recoverableConnectionManager.addConnection(si);
        }
    }

    public void writeToRandomRole(int role, Object msg) {
        cache.getRoleConnection(role).write(msg);
    }

    public void deliver(String module, String version, Object msg) {
        cache.getModuleConnection(module, version).write(msg);
    }

    public void updateRoutes(List<GameRoute> list) {
        cache.updateRoutes(list);
    }

    public void msgHandler(Handler<Object> handler) {
        msgHandler = handler;
    }

    public JsonObject info() {
        return recoverableConnectionManager.info();
    }

    private class ConnectionCache {

        Map<Integer, Integer> rolePreferConns = new HashMap<>();    // role -> id
        Map<Integer, Integer> activeConns = new HashMap<>();        // id -> role
        Map<String, GameRouteSelector> moduleConns = new HashMap<>();     // (module,version) -> id
        Map<Integer, List<Handler<Boolean>>> listeners = new HashMap<>();

        public RecoverableConnection getModuleConnection(String module, String version) {
            GameRouteSelector selector = moduleConns.get(module + "_" + version);
            if (selector == null) {
                throw ExceptionDefine.NOT_FOUND.build();
            }
            return getConnection(selector.get());
        }

        public RecoverableConnection getRoleConnection(int role) {
            return getConnection(rolePreferConns.getOrDefault(role, NON_SERVER_ID));
        }

        private RecoverableConnection getConnection(int id) {
            if (id == NON_SERVER_ID) {
                throw ExceptionDefine.NOT_FOUND.build();
            }
            RecoverableConnection conn = recoverableConnectionManager.get(id);

            if (conn == null) {
                throw ExceptionDefine.NOT_FOUND.build();
            }

            return conn;
        }

        public void add(int role, int serverId) {
            activeConns.put(serverId, role);
            if (CoreXUtil.isRole(role, ConstDefine.ROLE_AUTH)) {
                rolePreferConns.put(ConstDefine.ROLE_AUTH, serverId);
            }
            if (CoreXUtil.isRole(role, ConstDefine.ROLE_BROADCAST)) {
                rolePreferConns.put(ConstDefine.ROLE_BROADCAST, serverId);
            }

            listeners.getOrDefault(serverId, Collections.emptyList()).forEach(h -> h.handle(true));
        }

        public void remove(int role, int serverId) {
            if (activeConns.remove(serverId) != null) {
                if (CoreXUtil.isRole(role, ConstDefine.ROLE_AUTH)
                        && rolePreferConns.getOrDefault(ConstDefine.ROLE_AUTH, NON_SERVER_ID) == serverId) {
                    updateRoleConns(ConstDefine.ROLE_AUTH);
                }
                if (CoreXUtil.isRole(role, ConstDefine.ROLE_BROADCAST)
                        && rolePreferConns.getOrDefault(ConstDefine.ROLE_BROADCAST, NON_SERVER_ID) == serverId) {
                    updateRoleConns(ConstDefine.ROLE_BROADCAST);
                }

                listeners.getOrDefault(serverId, Collections.emptyList()).forEach(h -> h.handle(false));
            }
        }

        private void updateRoleConns(int role) {
            List<Integer> ret = new ArrayList<>(activeConns.size());
            for (Map.Entry<Integer, Integer> entry : activeConns.entrySet()) {
                if (role == entry.getValue()) {
                    ret.add(entry.getKey());
                }
            }

            if (ret.isEmpty()) {
                rolePreferConns.put(role, NON_SERVER_ID);
            } else {
                int serverId = RandomUtil.randomOneInList(ret);
                rolePreferConns.put(role, serverId);
            }
        }

        public void updateRoutes(List<GameRoute> list) {
            listeners.clear();
            for (GameRoute gr : list) {
                moduleConns.computeIfAbsent(uniqueName(gr.getModule(), gr.getVersion()), t -> new GameRouteSelector())
                        .update(gr);
            }
        }

        private class GameRouteSelector {

            List<Integer> candidates;
            boolean[] isActive;
            int current;

            public GameRouteSelector() {
                init();
            }

            private void init() {
                candidates = Collections.emptyList();
                isActive = new boolean[0];
                current = NON_SERVER_ID;
            }

            public void update(GameRoute gameRoute) {
                List<Integer> newCandidates = gameRoute.getCandidateIds();

                if (newCandidates.isEmpty()) {
                    init();
                } else {
                    boolean notChanged = notChanged(candidates, newCandidates);

                    this.candidates = newCandidates;
                    int sz = newCandidates.size();
                    this.isActive = new boolean[sz];

                    for (int i = 0; i < sz; ++i) {
                        int index = i;
                        int id = newCandidates.get(i);
                        isActive[i] = activeConns.containsKey(id);
                        listeners.computeIfAbsent(id, t -> new ArrayList<>(8)).add(b -> {
                            isActive[index] = b;
                            if (b && current == NON_SERVER_ID) {
                                current = id;
                            } else if (id == current) {
                                current = next();
                            }
                        });
                    }
                    this.current = notChanged ? current : next();
                }
            }

            private int next() {
                int sz = this.candidates.size();
                List<Integer> list = new ArrayList<>(sz);
                for (int i = 0; i < sz; ++i) {
                    if (isActive[i]) {
                        list.add(this.candidates.get(i));
                    }
                }
                if (list.isEmpty()) {
                    return NON_SERVER_ID;
                } else {
                    return RandomUtil.randomOneInList(list);
                }
            }

            public int get() {
                return current;
            }
        }
    }

    private static boolean notChanged(List<Integer> oldV, List<Integer> newV) {
        if (oldV.size() != newV.size()) {
            return false;
        }
        Set<Integer> s = new HashSet<>(3);
        s.addAll(oldV);
        return s.containsAll(newV);
    }

    private static String uniqueName(String module, String version) {
        return module + "_" + version;
    }

}
