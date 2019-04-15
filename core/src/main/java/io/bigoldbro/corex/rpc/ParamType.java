package io.bigoldbro.corex.rpc;

/**
 * Created by Joshua on 2019-04-10.
 */
public enum ParamType {
    UNSUPPORTED,

    BOOLEAN,
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    STRING,
    NET_DATA,

    ARRAY,     // 非原始类型 e.g Boolean[] Short[]
    LIST,      // 元素只能是非容器类型
    MAP,       // k:string, v:非容器类型
}
