-- --------------------------------------------------------
-- 主机:                           my-docker
-- Server version:               8.0.15 - MySQL Community Server - GPL
-- Server OS:                    Linux
-- HeidiSQL 版本:                  10.1.0.5464
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping structure for table corex.t_corex_node
CREATE TABLE IF NOT EXISTS `t_corex_node` (
  `server_id` int(11) unsigned NOT NULL,
  `description` varchar(50) NOT NULL DEFAULT '',
  `role` int(11) NOT NULL DEFAULT '0' COMMENT '角色',
  `host` varchar(50) NOT NULL,
  `port` int(11) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务器节点一览';

-- Dumping data for table corex.t_corex_node: ~4 rows (approximately)
/*!40000 ALTER TABLE `t_corex_node` DISABLE KEYS */;
INSERT INTO `t_corex_node` (`server_id`, `description`, `role`, `host`, `port`, `create_time`, `update_time`) VALUES
	(1, '网关,广播', 3, '192.168.92.160', 8001, '2018-04-04 13:59:20', '2018-08-29 10:20:01'),
	(2, '网关', 1, '192.168.92.160', 8002, '2018-04-04 13:59:20', '2018-08-29 10:20:01'),
	(11, '登录', 8, '192.168.92.160', 8011, '2018-04-04 13:59:20', '2018-08-29 10:20:01'),
	(21, '游戏', 4, '192.168.92.160', 8021, '2018-04-04 13:59:20', '2018-08-29 10:20:01');
/*!40000 ALTER TABLE `t_corex_node` ENABLE KEYS */;

-- Dumping structure for table corex.t_corex_route
CREATE TABLE IF NOT EXISTS `t_corex_route` (
  `id` int(11) NOT NULL,
  `module` varchar(50) NOT NULL,
  `version` varchar(50) NOT NULL COMMENT '版本号',
  `load_balance` smallint(6) NOT NULL COMMENT '负载均衡策略',
  `server_ids` varchar(255) NOT NULL COMMENT '服务器id(逗号分隔)',
  `ext_infos` varchar(255) NOT NULL COMMENT '服务器信息(逗号分隔)',
  `is_active` tinyint(4) NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `module_version` (`module`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='模块路由表';

-- Dumping data for table corex.t_corex_route: ~0 rows (approximately)
/*!40000 ALTER TABLE `t_corex_route` DISABLE KEYS */;
INSERT INTO `t_corex_route` (`id`, `module`, `version`, `load_balance`, `server_ids`, `ext_infos`, `is_active`, `create_time`, `update_time`) VALUES
	(1, 'demo', '1.0', 0, '21', '', 1, '2018-04-04 14:31:45', '2019-03-23 23:11:22');
/*!40000 ALTER TABLE `t_corex_route` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
