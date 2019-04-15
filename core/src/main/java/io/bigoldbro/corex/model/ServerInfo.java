package io.bigoldbro.corex.model;

import io.bigoldbro.corex.NetData;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.Objects;

/**
 * Created by Joshua on 2018/3/20.
 */
public class ServerInfo implements NetData {

    public static final int NON_SERVER_ID = 0;

    // 必须为正数
    private int serverId;
    private int role;
    private String host;
    private int port;

    public ServerInfo() {
    }

    public ServerInfo(int serverId, int role, String host, int port) {
        this.serverId = serverId;
        this.role = role;
        this.host = host;
        this.port = port;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerInfo that = (ServerInfo) o;
        return serverId == that.serverId &&
                role == that.role &&
                port == that.port &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId, role, host, port);
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "serverId=" + serverId +
                ", role=" + role +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public void read(DataInput dataInput) throws Exception {
        serverId = dataInput.readInt();
        role = dataInput.readInt();
        host = dataInput.readUTF();
        port = dataInput.readInt();
    }

    @Override
    public void write(DataOutput dataOutput) throws Exception {
        dataOutput.writeInt(serverId);
        dataOutput.writeInt(role);
        dataOutput.writeUTF(host);
        dataOutput.writeInt(port);
    }
}
