package io.bigoldbro.corex.rpc;

/**
 * Created by Joshua on 2019-04-10.
 */
public class ParamDetail {
    final ParamType type;
    final ParamType genericType;
    final Class<?> extClz;

    ParamDetail(ParamType type, ParamType genericType, Class<?> extClz) {
        this.type = type;
        this.genericType = genericType;
        this.extClz = extClz;
    }

}
