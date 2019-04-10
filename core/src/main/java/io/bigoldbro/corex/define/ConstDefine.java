package io.bigoldbro.corex.define;

/**
 * Created by Joshua on 2018/2/22.
 */
public class ConstDefine {

    /******************************** start                ********************************/

    public static final String DEFAULT_MODULE_PACKAGE = "corex.module";

    /******************************** 服务器功能定义         ********************************/
    public static final int ROLE_LOCAL = 0;     // 自己

    public static final int ROLE_GATEWAY = 1;
    public static final int ROLE_BROADCAST = 1 << 1;
    public static final int ROLE_GAME = 1 << 2;
    public static final int ROLE_AUTH = 1 << 3;
    public static final int ROLE_ADMIN = 1 << 4;
    public static final int ROLE_ROBOT = 1 << 5;

    public static final int ROLE_SET1 = ROLE_GATEWAY | ROLE_GAME | ROLE_ROBOT;

    public static final int ROLE_ALL = -1;
    /******************************** auth type定义         ********************************/
    public static final int AUTH_TYPE_NON = 0;          // 未登录
    public static final int AUTH_TYPE_CLIENT = 1;       // 已登录
    public static final int AUTH_TYPE_ADMIN = 2;
    public static final int AUTH_TYPE_INTERNAL = 3;


    /******************************** end                  ********************************/
}
