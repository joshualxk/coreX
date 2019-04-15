package io.bigoldbro.corex;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Created by Joshua on 2018/2/28.
 */
public abstract class AbstractConnection implements Connection {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String id;
    private long createTime;
    private long lastActiveTime;

    private Handler<Connection> openHandler;
    private Handler<Void> errorHandler;
    private Handler<Void> recoverHandler;
    private Handler<Object> msgHandler;
    private Handler<Void> closeHandler;

    private ConnectionState connectionState = ConnectionState.ORIGIN;

    public AbstractConnection(String id) {
        this.id = id;
        this.createTime = System.currentTimeMillis();
    }

    protected static String channelId(Channel channel) {
        return channel.id().asLongText();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public long createTime() {
        return createTime;
    }

    protected void updateLastActiveTime() {
        lastActiveTime = System.currentTimeMillis();
    }

    @Override
    public long lastActiveTime() {
        return lastActiveTime;
    }

    @Override
    public void openHandler(Handler<Connection> handler) {
        openHandler = handler;
    }

    @Override
    public void errorHandler(Handler<Void> handler) {
        errorHandler = handler;
    }

    @Override
    public void recoverHandler(Handler<Void> handler) {
        recoverHandler = handler;
    }

    @Override
    public void msgHandler(Handler<Object> handler) {
        msgHandler = handler;
    }

    @Override
    public void closeHandler(Handler<Void> handler) {
        closeHandler = handler;
    }

    @Override
    public void onOpen() {
        changeState(ConnectionState.OPEN);
        if (openHandler != null) {
            try {
                openHandler.handle(this);
            } catch (Throwable e) {
                logger.warn("error in onOpen:", e);
            }
        }
    }

    @Override
    public void onError() {
        changeState(ConnectionState.ERROR);
        if (errorHandler != null) {
            try {
                errorHandler.handle(null);
            } catch (Throwable e) {
                logger.warn("error in onError:", e);
            }
        }
    }

    @Override
    public void onRecover() {
        changeState(ConnectionState.OPEN);
        if (recoverHandler != null) {
            try {
                recoverHandler.handle(null);
            } catch (Throwable e) {
                logger.warn("error in onRecover:", e);
            }
        }
    }

    @Override
    public void onMsg(Object msg) {
        updateLastActiveTime();
        if (msgHandler != null) {
            try {
                msgHandler.handle(msg);
            } catch (Throwable e) {
                logger.warn("error in onMsg:", e);
            }
        }
    }

    @Override
    public void onClose() {
        changeState(ConnectionState.CLOSE);
        if (closeHandler != null) {
            try {
                closeHandler.handle(null);
            } catch (Throwable e) {
                logger.warn("error in onClose:", e);
            }

        }
    }

    protected void changeState(ConnectionState state) {
        this.connectionState = state;
    }

    protected ConnectionState state() {
        return connectionState;
    }

    @Override
    public boolean isOpen() {
        return state() == ConnectionState.OPEN;
    }

    @Override
    public boolean isError() {
        return state() == ConnectionState.ERROR;
    }

    @Override
    public boolean isClose() {
        return state() == ConnectionState.CLOSE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractConnection that = (AbstractConnection) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
