package io.bigoldbro.corex.gateway;

/**
 * Created by Joshua on 2018/2/24.
 */
public class StartupGateway2 {

    public static void main(String[] args) {

        try {
//            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext-gateway2.xml");
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
//                coreX.startService(applicationContext.getBean(GatewayService.class), h);
//            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("success");
    }
}