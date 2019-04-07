package io.bigoldbro.corex.service;

import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.rpc.ModuleParams;
import io.bigoldbro.corex.rpc.RpcHandler;
import io.bigoldbro.corex.rpc.ServerModuleScanner;

/**
 * Created by Joshua on 2018/3/23.
 */
public abstract class SimpleModuleService extends AbstractProcessorService {

    private final ModuleParams moduleParams;

    public SimpleModuleService() {
        ModuleParams moduleParams = new ServerModuleScanner(this).parse(findModule(this.getClass()));
        this.moduleParams = moduleParams;
    }

    @Override
    protected RpcHandler getHandler(String name) {
        return moduleParams.getHandler(name);
    }

    @Override
    protected Module getModule() {
        return moduleParams.module();
    }


    public static Class<?> findModule(Class<?> clz) {
        Class<?> target = findModuleRecursively(clz);
        if (target == null) {
            throw new CoreException(clz.getName() + "类未实现module接口类");
        }
        return target;
    }

    private static Class<?> findModuleRecursively(Class<?> clz) {
        if (clz == null) {
            return null;
        }
        if (clz.isInterface()) {
            if (clz.getAnnotation(Module.class) != null) {
                return clz;
            }
        } else {
            for (Class<?> aClass : clz.getInterfaces()) {
                if (aClass.getAnnotation(Module.class) != null) {
                    return aClass;
                }
            }
        }
        return findModuleRecursively(clz.getSuperclass());
    }
}
