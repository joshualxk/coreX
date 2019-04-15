package io.bigoldbro.corex.rpc;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Joshua on 2018/3/15.
 */
public class MethodDetail {

    final Method method;
    final List<ParamDetail> params;
    final ReturnDetail returnDetail;

    MethodDetail(Method method, List<ParamDetail> params, ReturnDetail returnDetail) {
        this.method = method;
        this.params = params;
        this.returnDetail = returnDetail;
    }

    public String name() {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

}
