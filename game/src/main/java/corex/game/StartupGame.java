package corex.game;

import corex.core.CoreX;
import corex.core.CoreXConfig;
import corex.core.Starter;
import corex.core.impl.CoreXImpl;
import corex.core.service.*;
import corex.core.utils.CoreXUtil;
import corex.demo.DemoGameImpl;
import corex.game.service.GameModuleService;

/**
 * Created by Joshua on 2018/2/26.
 */
public class StartupGame {

    public static void main(String[] args) {

        try {
//            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext-game1.xml");
//
//            CoreXConfig coreXConfig = applicationContext.getBean(CoreXConfig.class);
//            CoreX coreX = new CoreXImpl(coreXConfig);
//
//            CoreXUtil.<Void>sync(h -> {
//                coreX.startService(applicationContext.getBean(DashboardService.class), h);
//            });
//            CoreXUtil.<Void>sync(h -> {
//                coreX.startService(applicationContext.getBean(LogService.class), h);
//            });
//            CoreXUtil.<Void>sync(h -> {
//                coreX.startService(applicationContext.getBean(HarborServerService.class), h);
//            });
//            CoreXUtil.<Void>sync(h -> {
//                coreX.startService(applicationContext.getBean(HarborClientService.class), h);
//            });
//            CoreXUtil.<Void>sync(h -> {
//                coreX.startService(applicationContext.getBean(CacheService.class), h);
//            });
//            CoreXUtil.<Void>sync(h -> {
//                coreX.startService(new GameModuleService(new DemoGameImpl()), h);
//            });
//            CoreXUtil.<Void>sync(h -> {
//                coreX.startService(new TestService(), h);
//            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("success");
    }
}
