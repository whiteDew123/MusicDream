-- =====================================================
-- Mod_songList 歌单微服务建表脚本
-- 数据库：musicdreamer（MySQL 8.x）
-- 说明：user 表由 Mod_login 模块维护，此处不重复创建。
--       如已存在同名表，IF NOT EXISTS 会自动跳过。
-- =====================================================

-- 歌单主表
CREATE TABLE IF NOT EXISTS `song_list` (
  `id`           INT          NOT NULL AUTO_INCREMENT COMMENT '歌单ID',
  `name`         VARCHAR(255)          DEFAULT NULL COMMENT '歌单名',
  `user_id`      INT                   DEFAULT NULL COMMENT '所属用户ID',
  `pic`          VARCHAR(255)          DEFAULT NULL COMMENT '封面图片URL',
  `introduction` VARCHAR(255)          DEFAULT NULL COMMENT '歌单简介',
  `create_date`  DATE                  DEFAULT NULL COMMENT '创建时间',
  `style`        VARCHAR(255)          DEFAULT NULL COMMENT '风格',
  PRIMARY KEY (`id`),
  KEY `idx_song_list_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单表';

-- 歌单-歌曲关联表（自增ID + UNIQUE约束）
CREATE TABLE IF NOT EXISTS `list_music` (
  `id`       INT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `music_id` INT NOT NULL COMMENT '音乐ID',
  `list_id`  INT NOT NULL COMMENT '歌单ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_music_list` (`music_id`, `list_id`),
  KEY `idx_list_music_list` (`list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单-歌曲关联表';

-- 用户收藏歌单表（自增ID + UNIQUE约束）
CREATE TABLE IF NOT EXISTS `likelist` (
  `id`      INT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `list_id` INT NOT NULL COMMENT '歌单ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_list` (`user_id`, `list_id`),
  KEY `idx_likelist_list` (`list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏歌单表';