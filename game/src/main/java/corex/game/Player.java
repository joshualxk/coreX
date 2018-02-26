package corex.game;

import corex.core.Moable;

/**
 * Created by Joshua on 2018/3/23.
 */
public interface Player extends Moable {

    String userId();

    String nickName();

    String icon();

    boolean isRobot();

    boolean isOnline();

    /**
     * 更新在线状态
     *
     * @param online
     * @return 返回true表示状态有变化
     */
    boolean setOnline(boolean online);

    void onOnline();

    void onOffline();

}
