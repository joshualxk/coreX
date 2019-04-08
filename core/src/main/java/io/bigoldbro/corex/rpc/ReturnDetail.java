package io.bigoldbro.corex.rpc;

/**
 * Created by Joshua on 2019-04-10.
 */
public class ReturnDetail {
    public final boolean isVoid;
    public final ParamType callbackType;
    public final Class<?> joClz;

    ReturnDetail(boolean isVoid, ParamType callbackType, Class<?> joClz) {
        this.isVoid = isVoid;
        this.callbackType = callbackType;
        this.joClz = joClz;
    }
}
