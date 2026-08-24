-- =====================================================
-- Mod_like 收藏模块微服务建表脚本
-- 数据库：musicdreamer（MySQL 8.x）
-- 说明：song_list / likelist 表由 Mod_songList 模块维护，此处不重复创建。
-- =====================================================

-- 用户收藏歌曲表（自增ID，含收藏时间）
CREATE TABLE IF NOT EXISTS `like_music` (
  `id`          INT  NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id`     INT  NOT NULL COMMENT '用户ID',
  `music_id`    INT  NOT NULL COMMENT '音乐ID',
  `create_date` DATE          DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  KEY `idx_like_music_user` (`user_id`),
  KEY `idx_like_music_music` (`music_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏歌曲表';