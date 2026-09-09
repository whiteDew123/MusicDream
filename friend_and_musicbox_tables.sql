-- ========================================
-- 好友模块 & 盲盒模块 建表语句
-- 数据库：musicdreamer
-- 生成时间：2026-08-28
-- ========================================

-- ========================
-- 好友模块
-- ========================

-- 好友关系表
CREATE TABLE IF NOT EXISTS `friend` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `friend_id` INT NOT NULL COMMENT '好友ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成为好友时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_friend_id` (`friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 好友请求表
CREATE TABLE IF NOT EXISTS `friend_request` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sender_id` INT NOT NULL COMMENT '发送者ID',
  `receiver_id` INT NOT NULL COMMENT '接收者ID',
  `message` VARCHAR(200) DEFAULT NULL COMMENT '验证消息',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理 1-已接受 2-已拒绝',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sender_receiver` (`sender_id`, `receiver_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友请求表';


-- ========================
-- 盲盒模块
-- ========================

-- 音乐盲盒表
CREATE TABLE IF NOT EXISTS `music_box` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT NOT NULL COMMENT '创建者ID',
  `title` VARCHAR(100) NOT NULL COMMENT '盲盒标题',
  `mood_tag` VARCHAR(50) NOT NULL COMMENT '心情标签',
  `message` TEXT COMMENT '创建者留言（开启后可见）',
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面URL',
  `open_count` INT NOT NULL DEFAULT 0 COMMENT '开启次数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_like_count` (`like_count`),
  KEY `idx_open_count` (`open_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐盲盒表';

-- 盲盒歌曲关联表
CREATE TABLE IF NOT EXISTS `music_box_song` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `box_id` INT NOT NULL COMMENT '盲盒ID',
  `song_id` INT NOT NULL COMMENT '歌曲ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  PRIMARY KEY (`id`),
  KEY `idx_box_id` (`box_id`),
  KEY `idx_song_id` (`song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盲盒歌曲关联表';

-- 盲盒点赞表
CREATE TABLE IF NOT EXISTS `music_box_like` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `box_id` INT NOT NULL COMMENT '盲盒ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_box_user` (`box_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盲盒点赞表';

-- 盲盒开启记录表
CREATE TABLE IF NOT EXISTS `music_box_open_record` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `box_id` INT NOT NULL COMMENT '盲盒ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开启时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_box_user` (`box_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盲盒开启记录表';

-- 盲盒交友请求表
CREATE TABLE IF NOT EXISTS `music_box_friend_request` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `box_id` INT NOT NULL COMMENT '盲盒ID',
  `sender_id` INT NOT NULL COMMENT '发送者ID',
  `receiver_id` INT NOT NULL COMMENT '接收者ID',
  `message` VARCHAR(200) DEFAULT NULL COMMENT '交友留言',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理 1-已接受 2-已拒绝',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_box_sender_receiver` (`box_id`, `sender_id`, `receiver_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盲盒交友请求表';