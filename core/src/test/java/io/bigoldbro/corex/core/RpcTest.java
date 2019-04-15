package io.bigoldbro.corex.core;

import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.define.ConstDefine;
import io.bigoldbro.corex.module.AsyncModule;
import io.bigoldbro.corex.proto.Base;
import io.bigoldbro.corex.rpc.StandaloneClient;
import io.bigoldbro.corex.model.ServerInfo;
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

        Base.Auth auth1 = Base.Auth.newBuilder()
                .setType(ConstDefine.AUTH_TYPE_ADMIN)
                .build();

        Base.Auth auth2 = Base.Auth.newBuilder()
                .setType(ConstDefine.AUTH_TYPE_NON)
                .build();
        System.out.println(standaloneClient.connect(s.getHost(), s.getPort(), auth1, HarborClientModule.class).info());

        Future<String> future = standaloneClient.connect(s.getHost(), s.getPort(), auth2, AsyncModule.class).async(5000);

        future.addHandler(ar -> {
            if (ar.succeeded()) {
                System.out.println(ar.result());
            } else {
                System.out.println(ar.cause());
            }
        });

        try {
            System.out.println(standaloneClient.connect(s.getHost(), s.getPort(), auth2, AsyncModule.class).sync(234));
        } catch (Exception e) {
            e.printStackTrace();
        }

        future.sync(2000);
    }

}
