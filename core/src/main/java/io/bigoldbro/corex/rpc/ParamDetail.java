package io.bigoldbro.corex.rpc;

import java.util.Objects;

/**
 * Created by Joshua on 2019-04-10.
 */
public class ParamDetail {
    public final ParamType type;
    public final ParamType genericType;
    public final Class<?> joClz;

    private ParamDetail(ParamType type, ParamType genericType, Class<?> joClz) {
        this.type = type;
        this.genericType = genericType;
        this.joClz = joClz;
    }

    static class ParamDetailBuilder {
        private ParamType type;
        private ParamType genericType = ParamType.UNSUPPORTED;
        private Class<?> joClz;

        public void setType(ParamType type) {
            this.type = type;
        }

        public void setGenericType(ParamType genericType) {
            this.genericType = genericType;
        }

        public void setJoClz(Class<?> joClz) {
            this.joClz = joClz;
        }

        public ParamDetail build() {
            return new ParamDetail(type, genericType, joClz);
        }
    }
}
