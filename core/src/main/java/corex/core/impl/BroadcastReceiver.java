package corex.core.impl;

import corex.core.Context;
import corex.core.ContextAware;
import corex.core.Handler;
import corex.core.exception.CoreException;
import corex.core.model.Broadcast;
import corex.core.rpc.ModuleParams;
import corex.core.rpc.ServerModuleScanner;
import corex.module.BroadcastModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Joshua on 2018/4/3.
 */
public abstract class BroadcastReceiver implements ContextAware, BroadcastModule {

    protected static Logger logger = LoggerFactory.getLogger(BroadcastReceiver.class);

    private final boolean internal;
    private final boolean external;
    private final ModuleParams moduleParams;

    private Context context;

    public BroadcastReceiver(boolean internal, boolean external) {
        this.internal = internal;
        this.external = external;
        try {
            this.moduleParams = new ServerModuleScanner(this).parse(BroadcastModule.class);
        } catch (Exception e) {
            throw new CoreException("初始化广播模块失败");
        }
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public void init(Context context) {
        this.context = context;

        int need = 0;
        Handler<Broadcast> h1 = null;
        Handler<Broadcast> h2 = null;
        if (internal) {
            h1 = this::internalBroadcast;
            need++;
        }
        if (external) {
            h2 = this::broadcast2Users;
            need++;
        }

        if (context.coreX().subscribeBroadcast(h1, h2) != need) {
            throw new CoreException("订阅广播失败");
        }
    }

    protected int broadcast2Users(Broadcast broadcast) {
        return 0;
    }

    private void internalBroadcast(Broadcast broadcast) {
        try {
            moduleParams.handleInternalBroadcast(broadcast);
        } catch (Exception e) {
            logger.warn("处理广播错误.", e);
        }
    }
}
