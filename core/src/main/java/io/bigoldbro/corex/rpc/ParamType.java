package io.bigoldbro.corex.rpc;

/**
 * Created by Joshua on 2019-04-10.
 */
public enum ParamType {
    UNSUPPORTED,

    BOOLEAN,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    STRING,
    PROTO,

    RAW_ARRAY, // 原始类型 e.g. boolean[] short[]
    ARRAY,     // 非原始类型 e.g Boolean[] Short[]
    LIST,
}
