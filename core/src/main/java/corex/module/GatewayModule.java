package corex.module;

import corex.core.FutureMo;
import corex.core.annotation.Api;
import corex.core.annotation.Module;
import corex.core.define.ConstDefine;
import corex.core.define.ServiceNameDefine;

/**
 * Created by Joshua on 2018/3/29.
 */
@Module(address = ServiceNameDefine.GATEWAY)
public interface GatewayModule {

    @Api(value = "i", type = ConstDefine.AUTH_TYPE_ADMIN)
    FutureMo info();
}
