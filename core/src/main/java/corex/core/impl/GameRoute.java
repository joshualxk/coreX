package corex.core.impl;

import corex.core.Mo;
import corex.core.Moable;

import java.util.ArrayList;
import java.util.List;

import static corex.core.impl.ServerInfo.NON_SERVER_ID;

/**
 * Created by Joshua on 2018/4/8.
 */
public class GameRoute implements Moable {

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

    public static GameRoute fromMo(Mo mo) {
        GameRoute gameRoute = new GameRoute();
        gameRoute.setId(mo.getInt("id"));
        gameRoute.setModule(mo.getString("module"));
        gameRoute.setVersion(mo.getString("version"));
        gameRoute.setServerId1(mo.getInt("serverId1"));
        gameRoute.setServerId2(mo.getInt("serverId2"));
        gameRoute.setServerId3(mo.getInt("serverId3"));
        return gameRoute;
    }

    @Override
    public Mo toMo() {
        Mo mo = Mo.mo();
        mo.putInt("id", id);
        mo.putString("module", module);
        mo.putString("version", version);
        mo.putInt("serverId1", serverId1);
        mo.putInt("serverId2", serverId2);
        mo.putInt("serverId3", serverId3);
        return mo;
    }
}
