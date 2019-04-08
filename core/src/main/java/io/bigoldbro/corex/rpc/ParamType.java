package io.bigoldbro.corex.rpc;

/**
 * Created by Joshua on 2019-04-10.
 */
public enum ParamType {
    UNSUPPORTED,
    BOOLEAN,
    INT,
    LONG,
    DOUBLE,
    STRING,
    JO,
    JOABLE,

    RAW_ARRAY, // 原始类型
    ARRAY,
    LIST,
    JA,
}
