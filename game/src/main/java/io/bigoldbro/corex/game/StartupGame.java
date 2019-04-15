package io.bigoldbro.corex.game;

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
//                coreX.startService(new AsyncService(), h);
//            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("success");
    }
}
