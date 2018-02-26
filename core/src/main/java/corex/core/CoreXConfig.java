package corex.core;

/**
 * Created by Joshua on 2018/2/28.
 */
public class CoreXConfig {
    private int id;
    private int role;
    private int harborPort;
    private int httpPort;
    private String webRoot;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getHarborPort() {
        return harborPort;
    }

    public void setHarborPort(int harborPort) {
        this.harborPort = harborPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public String getWebRoot() {
        return webRoot;
    }

    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public String toString() {
        return "CoreXConfig{" +
                "id=" + id +
                ", role=" + role +
                ", harborPort=" + harborPort +
                ", httpPort=" + httpPort +
                ", webRoot='" + webRoot + '\'' +
                '}';
    }
}
