package corex.login.service;

import corex.core.FutureMo;
import corex.core.service.SimpleModuleService;
import corex.module.LoginModule;

import java.util.List;

/**
 * Created by Joshua on 2018/3/15.
 */
public class LoginService extends SimpleModuleService implements LoginModule {

    @Override
    public FutureMo info() {
        return baseInfo();
    }

    @Override
    public FutureMo authorize(String token) {
        FutureMo ret = FutureMo.futureMo();
        ret.putString("userId", token);
        return ret;
    }

    @Override
    public FutureMo getUser(String userId) {
        return FutureMo.futureMo();
    }

    @Override
    public FutureMo pay(String userId, String tradeNo, List<Integer> types, List<Integer> amounts, boolean income, String itemEvent, String comments) {
        return FutureMo.futureMo();
    }

    @Override
    public FutureMo pushAppMsg(List<String> userIds, String title, String info, String url) {
        System.out.println("userIds " + userIds);
        System.out.println("title " + title);
        System.out.println("info " + info);
        System.out.println("url " + url);
        return FutureMo.futureMo();
    }

    @Override
    public FutureMo queryUserAgent(String userId) {
        return FutureMo.futureMo();
    }

}
