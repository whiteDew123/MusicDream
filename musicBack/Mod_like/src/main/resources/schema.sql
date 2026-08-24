-- =====================================================
-- Mod_like 收藏模块微服务建表脚本
-- 数据库：musicdreamer（MySQL 8.x）
-- 说明：song_list 表由 Mod_songList 模块维护，此处不重复创建。
--       likelist 表与 Mod_songList 共享，IF NOT EXISTS 会自动跳过。
-- =====================================================

-- 用户收藏歌曲表（复合主键：music + user）
CREATE TABLE IF NOT EXISTS `mylike` (
  `music`       INT NOT NULL COMMENT '歌曲ID（关联 music.music_id）',
  `user`        INT NOT NULL COMMENT '用户ID（关联 user.id）',
  PRIMARY KEY (`music`, `user`),
  KEY `idx_mylike_user` (`user`),
  CONSTRAINT `fk_like_music` FOREIGN KEY (`music`) REFERENCES `music` (`music_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 用户收藏歌单表（与 Mod_songList 共享，已存在则跳过）
CREATE TABLE IF NOT EXISTS `likelist` (
  `userId`      INT NOT NULL COMMENT '用户ID（关联 user.id）',
  `listid`      INT NOT NULL COMMENT '歌单ID（关联 song_list.id）',
  PRIMARY KEY (`userId`, `listid`),
  KEY `idx_likelist_list` (`listid`),
  CONSTRAINT `fk_ll_list` FOREIGN KEY (`listid`) REFERENCES `song_list` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ll_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏歌单表';