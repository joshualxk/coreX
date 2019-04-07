package io.bigoldbro.corex.define;

/**
 * Created by Joshua on 2018/2/23.
 */
public final class ServiceNameDefine {

    public static final String INTERNAL_SERVICE_PREFIX = "s_";

    // nowhere
    public static final String NOWHERE = INTERNAL_SERVICE_PREFIX + "non";

    // 网关
    public static final String GATEWAY = INTERNAL_SERVICE_PREFIX + "gw";

    // 转发服务
    public static final String HARBOR_CLIENT = INTERNAL_SERVICE_PREFIX + "hb_c";
    public static final String HARBOR_SERVER = INTERNAL_SERVICE_PREFIX + "hb_s";

    // 广播
    public static final String BROADCAST = INTERNAL_SERVICE_PREFIX + "bc";

    // 登录
    public static final String LOGIN = INTERNAL_SERVICE_PREFIX + "lo";

    // 系统状态
    public static final String DASHBOARD = INTERNAL_SERVICE_PREFIX + "dab";

    // cache
    public static final String CACHE = INTERNAL_SERVICE_PREFIX + "cache";

    // 日志
    public static final String LOG = INTERNAL_SERVICE_PREFIX + "log";

    // 特殊定义
    public static final String SPECIAL = "x";

    public static boolean isValidName(String name) {
        return name != null && name.length() > 3;
    }

    public static boolean isInternalName(String name) {
        return name.startsWith(INTERNAL_SERVICE_PREFIX);
    }
}
