package io.bigoldbro.corex.rpc;

/**
 * Created by Joshua on 2019-04-10.
 */
public class ReturnDetail extends ParamDetail {
    final boolean isVoid;
    final boolean isAsync;

    ReturnDetail(ParamType type, ParamType genericType, Class<?> extClz, boolean isVoid, boolean isAsync) {
        super(type, genericType, extClz);
        this.isVoid = isVoid;
        this.isAsync = isAsync;
    }
}
