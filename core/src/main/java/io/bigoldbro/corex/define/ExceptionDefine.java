package io.bigoldbro.corex.define;

import io.bigoldbro.corex.exception.BizExceptionBuilder;

/**
 * Created by Joshua on 2018/2/23.
 */
public final class ExceptionDefine {

    // 1 ~ 999 为不可忽略错误
    public static final BizExceptionBuilder SYSTEM_BUSY = new BizExceptionBuilder(1, "系统繁忙");
    public static final BizExceptionBuilder SYSTEM_ERR = new BizExceptionBuilder(2, "系统错误");
    public static final BizExceptionBuilder PARAM_ERR = new BizExceptionBuilder(3, "传入参数有误");
    public static final BizExceptionBuilder RETRY = new BizExceptionBuilder(4, "服务暂不可用,请稍后再试");
    public static final BizExceptionBuilder NOT_AUTHORIZED = new BizExceptionBuilder(5, "没有权限访问");
    public static final BizExceptionBuilder WRONG_CONFIG = new BizExceptionBuilder(6, "配置错误");
    public static final BizExceptionBuilder WRONG_LOGIC = new BizExceptionBuilder(7, "逻辑错误");
    public static final BizExceptionBuilder TIME_OUT = new BizExceptionBuilder(8, "请求超时");
    public static final BizExceptionBuilder NOT_FOUND = new BizExceptionBuilder(9, "服务不存在");

    public static final BizExceptionBuilder NOT_LOGIN = new BizExceptionBuilder(100, "尚未登录");
    public static final BizExceptionBuilder DUPLICATE_LOGIN = new BizExceptionBuilder(101, "重复登录");
    public static final BizExceptionBuilder ALREADY_LOGIN = new BizExceptionBuilder(102, "已经登录");
    public static final BizExceptionBuilder CONN_FAIL = new BizExceptionBuilder(103, "连接失败");

}
