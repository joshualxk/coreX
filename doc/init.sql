-- --------------------------------------------------------
-- 主机:                           192.168.92.180
-- 服务器版本:                        5.6.24-enterprise-commercial-advanced-log - MySQL Enterprise Server - Advanced Edition (Commercial)
-- 服务器操作系统:                      linux-glibc2.5
-- HeidiSQL 版本:                  9.5.0.5196
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- 导出  表 h5game.t_game_node 结构
CREATE TABLE IF NOT EXISTS `t_game_node` (
  `server_id` int(11) NOT NULL,
  `description` varchar(50) NOT NULL,
  `role` int(11) NOT NULL DEFAULT '0' COMMENT '角色',
  `host` varchar(50) NOT NULL,
  `port` int(11) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务器节点一览';

-- 正在导出表  h5game.t_game_node 的数据：~4 rows (大约)
/*!40000 ALTER TABLE `t_game_node` DISABLE KEYS */;
INSERT INTO `t_game_node` (`server_id`, `description`, `role`, `host`, `port`, `create_time`, `update_time`) VALUES
	(1, '网关,广播', 3, '192.168.92.160', 8001, '2018-04-04 13:59:20', '2018-08-29 10:20:01'),
	(2, '网关', 1, '192.168.92.160', 8002, '2018-04-04 13:59:20', '2018-08-29 10:20:01'),
	(11, '登录', 8, '192.168.92.160', 8011, '2018-04-04 13:59:20', '2018-08-29 10:20:01'),
	(21, '游戏', 4, '192.168.92.160', 8021, '2018-04-04 13:59:20', '2018-08-29 10:20:01');
/*!40000 ALTER TABLE `t_game_node` ENABLE KEYS */;

-- 导出  表 h5game.t_game_route 结构
CREATE TABLE IF NOT EXISTS `t_game_route` (
  `id` int(11) NOT NULL,
  `module` varchar(50) NOT NULL,
  `version` varchar(50) NOT NULL,
  `server_id1` int(11) NOT NULL DEFAULT '0' COMMENT '服务器节点1',
  `server_id2` int(11) NOT NULL DEFAULT '0' COMMENT '服务器节点2',
  `server_id3` int(11) NOT NULL DEFAULT '0' COMMENT '服务器节点3',
  `is_active` tinyint(4) NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `module_version` (`module`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='模块路由表';

-- 正在导出表  h5game.t_game_route 的数据：~1 rows (大约)
/*!40000 ALTER TABLE `t_game_route` DISABLE KEYS */;
INSERT INTO `t_game_route` (`id`, `module`, `version`, `server_id1`, `server_id2`, `server_id3`, `is_active`, `create_time`, `update_time`) VALUES
	(1, 'demo', '1.0', 0, 21, 0, 1, '2018-04-04 14:31:45', '2018-08-28 17:24:49');
/*!40000 ALTER TABLE `t_game_route` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
