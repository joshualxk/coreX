package io.bigoldbro.corex.rpc;

import io.bigoldbro.corex.annotation.Module;
import io.bigoldbro.corex.json.JsonObjectImpl;
import io.bigoldbro.corex.model.Auth;
import io.bigoldbro.corex.model.Broadcast;
import io.bigoldbro.corex.model.Push;

import java.util.Map;
import java.util.Objects;

/**
 * Created by Joshua on 2018/2/27.
 */
public class ModuleParams {

    private final Module module;
    private final Map<String, RpcHandler> apiHandlers;

    public ModuleParams(Module module, Map<String, RpcHandler> apiHandlers) {
        this.module = module;
        this.apiHandlers = Objects.requireNonNull(apiHandlers, "apiHandlers");
    }

    public Module module() {
        return module;
    }

    public int size() {
        return apiHandlers.size();
    }

    public void handleInternalBroadcast(Broadcast broadcast) throws Exception {
        Push push = broadcast.getPush();
        JsonObjectImpl body = push.getBody();
        RpcHandler rpcHandler = getHandler(broadcast.getPush().getTopic());
        if (rpcHandler != null) {
            rpcHandler.handle(Auth.internalAuth(), body);
        }
    }

    public RpcHandler getHandler(String name) {
        return apiHandlers.get(name);
    }

    @Override
    public String toString() {
        return "ModuleParams{" +
                "module=" + module +
                ", apiHandlers=" + apiHandlers +
                '}';
    }
}
