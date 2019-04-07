package io.bigoldbro.corex.core;

import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.impl.ServerInfo;
import io.bigoldbro.corex.impl.StandaloneClient;
import io.bigoldbro.corex.module.HarborClientModule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joshua on 2018/3/27.
 */
public class RpcTest {

    @Test
    public void standaloneClient() throws Exception {
        List<ServerInfo> serverInfos = new ArrayList<>();
        serverInfos.add(new ServerInfo(1, ConstDefine.ROLE_BROADCAST | ConstDefine.ROLE_GATEWAY, "127.0.0.1", 8001));
        serverInfos.add(new ServerInfo(2, ConstDefine.ROLE_GATEWAY, "127.0.0.1", 8002));
        serverInfos.add(new ServerInfo(11, ConstDefine.ROLE_AUTH, "127.0.0.1", 8011));
        serverInfos.add(new ServerInfo(21, ConstDefine.ROLE_GAME, "127.0.0.1", 8021));

        StandaloneClient standaloneClient = new StandaloneClient(ConstDefine.DEFAULT_MODULE_PACKAGE, 55, 6000);

        ServerInfo s = serverInfos.get(0);

        System.out.println(standaloneClient.connect(s.getHost(), s.getPort(), HarborClientModule.class).info().jo());
    }

}
