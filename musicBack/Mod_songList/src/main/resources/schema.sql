-- =====================================================
-- Mod_songList 歌单微服务建表脚本
-- 数据库：musicdreamer（MySQL 8.x）
-- 说明：user 表由 Mod_login 模块维护，此处不重复创建。
--       如已存在同名表，IF NOT EXISTS 会自动跳过。
-- =====================================================

-- 歌单主表
CREATE TABLE IF NOT EXISTS `song_list` (
  `id`           INT          NOT NULL AUTO_INCREMENT COMMENT '歌单ID',
  `name`         VARCHAR(255)          DEFAULT NULL COMMENT '歌单名称',
  `pic`          VARCHAR(500)          DEFAULT NULL COMMENT '封面URL',
  `introduction` VARCHAR(2000)         DEFAULT NULL COMMENT '简介',
  `style`        VARCHAR(100)          DEFAULT NULL COMMENT '风格',
  `user_id`      INT                   DEFAULT NULL COMMENT '创建者用户ID（关联 user.id）',
  `create_date`  DATETIME              DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_song_list_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单表';

-- 歌单-歌曲关联表
CREATE TABLE IF NOT EXISTS `list_music` (
  `id`        INT NOT NULL AUTO_INCREMENT,
  `list_id`   INT DEFAULT NULL COMMENT '歌单ID（关联 song_list.id）',
  `music_id`  INT DEFAULT NULL COMMENT '歌曲ID（关联 music 表主键）',
  PRIMARY KEY (`id`),
  KEY `idx_list_music_list` (`list_id`),
  UNIQUE KEY `uk_list_music` (`list_id`, `music_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单-歌曲关联表';

-- 用户收藏歌单表
CREATE TABLE IF NOT EXISTS `likelist` (
  `id`        INT NOT NULL AUTO_INCREMENT,
  `user_id`   INT DEFAULT NULL COMMENT '用户ID（关联 user.id）',
  `list_id`   INT DEFAULT NULL COMMENT '歌单ID（关联 song_list.id）',
  PRIMARY KEY (`id`),
  KEY `idx_likelist_user` (`user_id`),
  UNIQUE KEY `uk_likelist` (`user_id`, `list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏歌单表';
