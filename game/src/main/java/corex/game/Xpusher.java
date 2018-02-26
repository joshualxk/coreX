package corex.game;

import corex.proto.ModelProto.Auth;
import corex.proto.ModelProto.Broadcast;

/**
 * Created by Joshua on 2018/3/27.
 */
public interface Xpusher {

    void before(Auth auth);

    String userId();

    void addBroadcast(Broadcast broadcast);

    void after(boolean success);
}
