package corex.login.service;

import corex.core.JoHolder;
import corex.core.service.SimpleModuleService;
import corex.module.LoginModule;

import java.util.List;

/**
 * Created by Joshua on 2018/3/15.
 */
public class LoginService extends SimpleModuleService implements LoginModule {

    @Override
    public JoHolder authorize(String token) {
        JoHolder ret = JoHolder.newSync();
        ret.jo().put("userId", token);
        return ret;
    }

    @Override
    public JoHolder getUser(String userId) {
        return JoHolder.newSync();
    }

    @Override
    public JoHolder pay(String userId, String tradeNo, List<Integer> types, List<Integer> amounts, boolean income, String itemEvent, String comments) {
        return JoHolder.newSync();
    }

    @Override
    public JoHolder pushAppMsg(List<String> userIds, String title, String info, String url) {
        System.out.println("userIds " + userIds);
        System.out.println("title " + title);
        System.out.println("info " + info);
        System.out.println("url " + url);
        return JoHolder.newSync();
    }

    @Override
    public JoHolder queryUserAgent(String userId) {
        return JoHolder.newSync();
    }

}
