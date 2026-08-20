-- =====================================================
-- Mod_like 收藏模块微服务建表脚本
-- 数据库：musicdreamer（MySQL 8.x）
-- 说明：song_list 表由 Mod_songList 模块维护，此处不重复创建。
--       likelist 表与 Mod_songList 共享，IF NOT EXISTS 会自动跳过。
-- =====================================================

-- 用户收藏歌曲表
CREATE TABLE IF NOT EXISTS `like_music` (
  `id`          INT NOT NULL AUTO_INCREMENT COMMENT '收藏记录ID',
  `user_id`     INT NOT NULL COMMENT '用户ID（关联 user.id）',
  `music_id`    INT NOT NULL COMMENT '歌曲ID（关联 music.id）',
  `create_date` DATETIME DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_like_music` (`user_id`, `music_id`),
  KEY `idx_like_music_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏歌曲表';

-- 用户收藏歌单表（与 Mod_songList 共享，已存在则跳过）
CREATE TABLE IF NOT EXISTS `likelist` (
  `id`        INT NOT NULL AUTO_INCREMENT,
  `user_id`   INT DEFAULT NULL COMMENT '用户ID（关联 user.id）',
  `list_id`   INT DEFAULT NULL COMMENT '歌单ID（关联 song_list.id）',
  PRIMARY KEY (`id`),
  KEY `idx_likelist_user` (`user_id`),
  UNIQUE KEY `uk_likelist` (`user_id`, `list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏歌单表';
