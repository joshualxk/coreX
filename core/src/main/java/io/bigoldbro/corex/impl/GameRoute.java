package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.json.JsonObjectImpl;

import java.util.ArrayList;
import java.util.List;

import static io.bigoldbro.corex.impl.ServerInfo.NON_SERVER_ID;

/**
 * Created by Joshua on 2018/4/8.
 */
public class GameRoute implements Joable {

    private int id;
    private String module;
    private String version;
    private int serverId1;
    private int serverId2;
    private int serverId3;
    private boolean isActive;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getServerId1() {
        return serverId1;
    }

    public void setServerId1(int serverId1) {
        this.serverId1 = serverId1;
    }

    public int getServerId2() {
        return serverId2;
    }

    public void setServerId2(int serverId2) {
        this.serverId2 = serverId2;
    }

    public int getServerId3() {
        return serverId3;
    }

    public void setServerId3(int serverId3) {
        this.serverId3 = serverId3;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Integer> getCandidateIds() {
        List<Integer> candidates = new ArrayList<>(3);
        if (getServerId1() != NON_SERVER_ID) {
            candidates.add(getServerId1());
        }
        if (getServerId2() != NON_SERVER_ID) {
            candidates.add(getServerId2());
        }
        if (getServerId3() != NON_SERVER_ID) {
            candidates.add(getServerId3());
        }
        return candidates;
    }

    @Override
    public String toString() {
        return "GameRoute{" +
                "id=" + id +
                ", module='" + module + '\'' +
                ", version='" + version + '\'' +
                ", serverId1=" + serverId1 +
                ", serverId2=" + serverId2 +
                ", serverId3=" + serverId3 +
                ", isActive=" + isActive +
                '}';
    }

    public static GameRoute fromJo(JsonObjectImpl jo) {
        GameRoute gameRoute = new GameRoute();
        gameRoute.setId(jo.getInteger("id"));
        gameRoute.setModule(jo.getString("m"));
        gameRoute.setVersion(jo.getString("v"));
        gameRoute.setServerId1(jo.getInteger("sId1"));
        gameRoute.setServerId2(jo.getInteger("sId2"));
        gameRoute.setServerId3(jo.getInteger("sId3"));
        return gameRoute;
    }

    @Override
    public JsonObjectImpl toJo() {
        return new JsonObjectImpl()
                .put("id", id)
                .put("m", module)
                .put("v", version)
                .put("sId1", serverId1)
                .put("sId2", serverId2)
                .put("sId3", serverId3);
    }
}
