package io.bigoldbro.corex.rpc;

import com.google.protobuf.GeneratedMessageV3;

/**
 * Created by Joshua on 2019-04-10.
 */
public class ParamDetail {
    public final ParamType type;
    public final ParamType genericType;
    public final Class<? extends GeneratedMessageV3> msgClz;

    private ParamDetail(ParamType type, ParamType genericType, Class<? extends GeneratedMessageV3> joClz) {
        this.type = type;
        this.genericType = genericType;
        this.msgClz = joClz;
    }

    static class ParamDetailBuilder {
        private ParamType type;
        private ParamType genericType = ParamType.UNSUPPORTED;
        private Class<? extends GeneratedMessageV3> msgClz;

        public void setType(ParamType type) {
            this.type = type;
        }

        public void setGenericType(ParamType genericType) {
            this.genericType = genericType;
        }

        public void setMsgClz(Class<? extends GeneratedMessageV3> msgClz) {
            this.msgClz = msgClz;
        }

        public ParamDetail build() {
            // TODO check params
            return new ParamDetail(type, genericType, msgClz);
        }
    }
}
