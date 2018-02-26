package corex.dao;

import corex.core.impl.GameRoute;
import corex.core.impl.ServerInfo;

import java.util.List;

/**
 * Created by Joshua on 2018/4/4.
 */
public interface BasicDao {

    List<ServerInfo> selectServerInfos();

    List<GameRoute> selectGameRoutes();

}
