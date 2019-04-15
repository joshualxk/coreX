package io.bigoldbro.corex.gateway;

import io.bigoldbro.corex.CoreX;
import io.bigoldbro.corex.Future;
import io.bigoldbro.corex.gateway.service.GatewayService;
import io.bigoldbro.corex.impl.CoreXConfig;
import io.bigoldbro.corex.impl.CoreXImpl;
import io.bigoldbro.corex.impl.FutureImpl;
import io.bigoldbro.corex.service.*;

/**
 * Created by Joshua on 2018/2/24.
 */
public class StartupGateway {

    public static void main(String[] args) {

        try {
            CoreX corex = new CoreXImpl(CoreXConfig.readConfig());

            Future<String> future;

            future = new FutureImpl<>();
            corex.startService(DashboardService.class, future);
            future.sync();

            future = new FutureImpl<>();
            corex.startService(LogService.class, future);
            future.sync();

            future = new FutureImpl<>();
            corex.startService(HarborServerService.class, future);
            future.sync();

            future = new FutureImpl<>();
            corex.startService(HarborClientService.class, future);
            future.sync();

            future = new FutureImpl<>();
            corex.startService(CacheService.class, future);
            future.sync();

            future = new FutureImpl<>();
            corex.startService(GatewayService.class, future);
            future.sync();

            future = new FutureImpl<>();
            corex.startService(AsyncService.class, future);
            future.sync();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("success");
    }
}
