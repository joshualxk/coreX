package io.bigoldbro.corex;

/**
 * Created by Joshua on 2018/3/20.
 */
public enum ConnectionState {
    ORIGIN,     // 初始状态
    OPEN,       // 已连接
    ERROR,      // 故障
    CLOSE,      // 已关闭
}
