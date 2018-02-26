package corex.core;

/**
 * Created by Joshua on 2018/2/28.
 */
public interface Connection {

    String id();

    long createTime();

    long lastActiveTime();

    void openHandler(Handler<Connection> handler);

    void errorHandler(Handler<Void> handler);

    void recoverHandler(Handler<Void> handler);

    void msgHandler(Handler<Object> handler);

    void closeHandler(Handler<Void> handler);

    void onOpen();

    void onError();

    void onRecover();

    void onMsg(Object msg);

    void onClose();

    void write(Object msg);

    void close();

    boolean isOpen();

    boolean isError();

    boolean isClose();

    boolean setSession(Session session);

    Session session();
}
