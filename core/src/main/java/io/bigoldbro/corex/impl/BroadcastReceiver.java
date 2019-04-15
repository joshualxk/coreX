package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.Context;
import io.bigoldbro.corex.ContextAware;
import io.bigoldbro.corex.Handler;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.module.BroadcastModule;
import io.bigoldbro.corex.proto.Base;
import io.bigoldbro.corex.rpc.ModuleInfo;
import io.bigoldbro.corex.rpc.ServerModuleScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Joshua on 2018/4/3.
 */
public abstract class BroadcastReceiver implements ContextAware, BroadcastModule {

    protected static Logger logger = LoggerFactory.getLogger(BroadcastReceiver.class);

    private final boolean internal;
    private final boolean external;
    private final ModuleInfo moduleInfo;

    private Context context;

    public BroadcastReceiver(boolean internal, boolean external) {
        this.internal = internal;
        this.external = external;
        try {
            this.moduleInfo = new ServerModuleScanner(this, BroadcastModule.class).parse();
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
        Handler<Base.Broadcast> h1 = null;
        Handler<Base.Broadcast> h2 = null;
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

    protected int broadcast2Users(Base.Broadcast broadcast) {
        return 0;
    }

    private void internalBroadcast(Base.Broadcast broadcast) {
        try {
            moduleInfo.handleInternalBroadcast(broadcast);
        } catch (Exception e) {
            logger.warn("处理广播错误.", e);
        }
    }
}
