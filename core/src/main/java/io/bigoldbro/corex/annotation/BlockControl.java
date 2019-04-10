package io.bigoldbro.corex.annotation;

/**
 * Created by Joshua on 2018/3/8.
 */
public enum BlockControl {
    NON_BLOCK,          // 非阻塞
    BLOCK,              // 阻塞
    MULTI_THREADED,     // 阻塞,多线程
}
