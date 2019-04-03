package corex.login;

import corex.core.CoreX;
import corex.core.CoreXConfig;
import corex.core.impl.CoreXImpl;
import corex.core.service.*;
import corex.core.utils.CoreXUtil;
import corex.login.service.LoginService;

/**
 * Created by Joshua on 2018/2/24.
 */
public class StartupLogin {

    public static void main(String[] args) {

        try {
//            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext-login1.xml");
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
//                coreX.startService(applicationContext.getBean(LoginService.class), h);
//            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("success");
    }
}
