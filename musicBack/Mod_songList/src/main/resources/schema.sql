-- =====================================================
-- Mod_songList 歌单微服务建表脚本
-- 数据库：musicdreamer（MySQL 8.x）
-- 说明：user 表由 Mod_login 模块维护，此处不重复创建。
--       如已存在同名表，IF NOT EXISTS 会自动跳过。
-- =====================================================

-- 歌单主表
CREATE TABLE IF NOT EXISTS `song_list` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '歌单ID',
  `name`        VARCHAR(255)          DEFAULT NULL COMMENT '歌单名称',
  `image`       VARCHAR(500)          DEFAULT NULL COMMENT '封面URL',
  `message`     VARCHAR(2000)         DEFAULT NULL COMMENT '简介',
  `user`        INT                   DEFAULT NULL COMMENT '创建者用户ID（关联 user.id）',
  `create_date` DATETIME              DEFAULT NULL COMMENT '创建时间',
  `tags`        VARCHAR(255)          DEFAULT NULL COMMENT '标签',
  PRIMARY KEY (`id`),
  KEY `idx_song_list_user` (`user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单表';

-- 歌单-歌曲关联表（复合主键：music + listid）
CREATE TABLE IF NOT EXISTS `list_music` (
  `music`       INT NOT NULL COMMENT '歌曲ID（关联 music.music_id）',
  `listid`      INT NOT NULL COMMENT '歌单ID（关联 song_list.id）',
  PRIMARY KEY (`music`, `listid`),
  KEY `idx_list_music_list` (`listid`),
  CONSTRAINT `fk_lm_list` FOREIGN KEY (`listid`) REFERENCES `song_list` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_lm_music` FOREIGN KEY (`music`) REFERENCES `music` (`music_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单-歌曲关联表';

-- 用户收藏歌单表（复合主键：userId + listid）
CREATE TABLE IF NOT EXISTS `likelist` (
  `userId`      INT NOT NULL COMMENT '用户ID（关联 user.id）',
  `listid`      INT NOT NULL COMMENT '歌单ID（关联 song_list.id）',
  PRIMARY KEY (`userId`, `listid`),
  KEY `idx_likelist_list` (`listid`),
  CONSTRAINT `fk_ll_list` FOREIGN KEY (`listid`) REFERENCES `song_list` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ll_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏歌单表';