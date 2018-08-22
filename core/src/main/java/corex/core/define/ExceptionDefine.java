package corex.core.define;

import corex.core.exception.BizException;
import corex.core.exception.BizExceptionBuilder;

/**
 * Created by Joshua on 2018/2/23.
 */
public final class ExceptionDefine {

    public static BizException newException(String msg) {
        return new BizException(-1000, msg);
    }

    public static BizException newException(int code, String msg) {
        return new BizException(code, msg);
    }

    // -1 ~ -999 为不可忽略错误
    public static final BizExceptionBuilder SYSTEM_BUSY = new BizExceptionBuilder(-67, "系统繁忙");
    public static final BizExceptionBuilder SYSTEM_ERR = new BizExceptionBuilder(-68, "系统错误");
    public static final BizExceptionBuilder PARAM_ERR = new BizExceptionBuilder(-69, "传入参数有误");
    public static final BizExceptionBuilder RETRY = new BizExceptionBuilder(-70, "服务暂不可用,请稍后再试");
    public static final BizExceptionBuilder NOT_AUTHORIZED = new BizExceptionBuilder(-71, "没有权限访问");
    public static final BizExceptionBuilder AUTH_FAIL = new BizExceptionBuilder(-72, "鉴权失败");
    public static final BizExceptionBuilder WRONG_CONFIG = new BizExceptionBuilder(-73, "配置错误");
    public static final BizExceptionBuilder WRONG_LOGIC = new BizExceptionBuilder(-74, "逻辑错误");
    public static final BizExceptionBuilder TIME_OUT = new BizExceptionBuilder(-75, "请求超时");
    public static final BizExceptionBuilder NOT_FOUND = new BizExceptionBuilder(-76, "服务不存在");
    public static final BizExceptionBuilder NOT_LOGIN = new BizExceptionBuilder(-77, "尚未登录");
    public static final BizExceptionBuilder DUPLICATE_LOGIN = new BizExceptionBuilder(-78, "重复登录");
    public static final BizExceptionBuilder ALREADY_LOGIN = new BizExceptionBuilder(-79, "已经登录");
    public static final BizExceptionBuilder GAME_CLOSING = new BizExceptionBuilder(-80, "游戏已关闭");
    public static final BizExceptionBuilder GAME_PLAYER_LIMIT = new BizExceptionBuilder(-81, "游戏人数已满");
    public static final BizExceptionBuilder GAME_HAS_BEGUN = new BizExceptionBuilder(-82, "游戏已开始");
    public static final BizExceptionBuilder NOT_IN_GAME = new BizExceptionBuilder(-83, "不在游戏中");

    //  -1000+为游戏逻辑错误
    public static final BizExceptionBuilder IN_ROOM = new BizExceptionBuilder(-1008, "你已在房间中");
    public static final BizExceptionBuilder NOT_IN_ROOM = new BizExceptionBuilder(-1009, "你不在房间中");
    public static final BizExceptionBuilder ROOM_NUM_LIMIT = new BizExceptionBuilder(-1010, "人数已满");
    public static final BizExceptionBuilder ROOM_NOT_MATCH = new BizExceptionBuilder(-1011, "找不到匹配房间");

}
