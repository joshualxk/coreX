package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.NetData;
import io.netty.util.internal.StringUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Joshua on 2018/4/8.
 */
public class GameRoute implements NetData {

    private int id;
    private String module;
    private String version;
    private String serverIds;
    private boolean isActive;

    private List<Integer> serverIdList;

    public int getId() {
        return id;
    }

    public String getModule() {
        return module;
    }

    public String getVersion() {
        return version;
    }

    public String getServerIds() {
        return serverIds;
    }

    public boolean isActive() {
        return isActive;
    }

    private static List<Integer> parseServerList(String serverIds) {
        if (!StringUtil.isNullOrEmpty(serverIds)) {
            String[] split = serverIds.split(",");
            List<Integer> ret = new ArrayList<>(split.length);
            try {
                for (String s : split) {
                    ret.add(Integer.valueOf(s));
                }
            } catch (Exception e) {

            }
            return Collections.unmodifiableList(ret);
        }
        return Collections.emptyList();
    }

    public List<Integer> getCandidateIds() {
        List<Integer> list = serverIdList;
        return list == null ? Collections.EMPTY_LIST : list;
    }

    @Override
    public String toString() {
        return "GameRoute{" +
                "id=" + id +
                ", module='" + module + '\'' +
                ", version='" + version + '\'' +
                ", serverIds='" + serverIds + '\'' +
                ", isActive=" + isActive +
                ", serverIdList=" + serverIdList +
                '}';
    }

    @Override
    public void read(DataInput dataInput) throws Exception {
        id = dataInput.readInt();
        module = dataInput.readUTF();
        version = dataInput.readUTF();
        serverIds = dataInput.readUTF();

        serverIdList = parseServerList(serverIds);
    }

    @Override
    public void write(DataOutput dataOutput) throws Exception {
        dataOutput.writeInt(id);
        dataOutput.writeUTF(module);
        dataOutput.writeUTF(version);
        dataOutput.writeUTF(serverIds);
    }
}
