-- =====================================================
-- Mod_room 一起听·播放室 建表脚本
-- 数据库：musicdreamer（MySQL 8.x）
-- 说明：user / music 表由既有模块维护，此处不重复创建。
--       room_* 为新增 5 张表，IF NOT EXISTS 避免重复执行报错。
-- =====================================================

-- 1. 房间主表
CREATE TABLE IF NOT EXISTS `room` (
  `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '房间ID',
  `name`                 VARCHAR(100) NOT NULL COMMENT '房间名称',
  `owner_id`             BIGINT                DEFAULT NULL COMMENT '房主用户ID',
  `max_members`          INT                   DEFAULT 5 COMMENT '人数上限（默认5，范围2-5）',
  `cover`                VARCHAR(255)          DEFAULT NULL COMMENT '房间封面URL（P3，预留）',
  `is_public`            TINYINT               DEFAULT 0 COMMENT '是否公开：0-私密 1-公开',
  `invite_code`          VARCHAR(8)            DEFAULT NULL COMMENT '邀请码（唯一）',
  `status`               TINYINT               DEFAULT 0 COMMENT '状态：0-空闲 1-播放中 2-已结束',
  `current_music_id`     BIGINT                DEFAULT NULL COMMENT '当前歌曲ID',
  `current_progress`     DOUBLE                DEFAULT 0 COMMENT '当前进度（秒）',
  `is_playing`           TINYINT               DEFAULT 0 COMMENT '是否播放中：0-暂停 1-播放',
  `play_mode`            TINYINT               DEFAULT 0 COMMENT '播放模式：0-循环播放 1-播放完毕停止',
  `invite_expire_hours`  INT                   DEFAULT 6 COMMENT '邀请码有效期（小时），范围 1-12',
  `create_time`          DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_invite_code` (`invite_code`),
  KEY `idx_room_owner` (`owner_id`),
  KEY `idx_room_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='一起听播放室房间表';

-- 2. 房间成员表
CREATE TABLE IF NOT EXISTS `room_member` (
  `id`             BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_id`        BIGINT  NOT NULL COMMENT '房间ID',
  `user_id`        BIGINT  NOT NULL COMMENT '用户ID',
  `role`           TINYINT DEFAULT 1 COMMENT '角色：0-房主 1-成员',
  `is_online`      TINYINT DEFAULT 1 COMMENT '在线状态：0-离线 1-在线',
  `last_heartbeat` DATETIME DEFAULT NULL COMMENT '最近心跳时间',
  `join_time`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_user` (`room_id`, `user_id`),
  KEY `idx_member_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间成员表';

-- 3. 房间歌单表
CREATE TABLE IF NOT EXISTS `room_playlist` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_id`    BIGINT NOT NULL COMMENT '房间ID',
  `music_id`   BIGINT NOT NULL COMMENT '歌曲ID',
  `added_by`   BIGINT DEFAULT NULL COMMENT '添加者用户ID',
  `sort_order` INT    DEFAULT 0 COMMENT '排序序号',
  `status`     TINYINT DEFAULT 0 COMMENT '状态：0-待播 1-播放中 2-已播',
  `add_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_music` (`room_id`, `music_id`),
  KEY `idx_playlist_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间歌单表';

-- 4. 切歌投票表
CREATE TABLE IF NOT EXISTS `room_playlist_vote` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_id`    BIGINT NOT NULL COMMENT '房间ID',
  `music_id`   BIGINT NOT NULL COMMENT '目标歌曲ID',
  `user_id`    BIGINT NOT NULL COMMENT '投票人ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间，用于30秒超时判断',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_music_user` (`room_id`, `music_id`, `user_id`),
  KEY `idx_vote_room_music` (`room_id`, `music_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='切歌投票表';

-- 5. 房间消息表
CREATE TABLE IF NOT EXISTS `room_message` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_id`     BIGINT NOT NULL COMMENT '房间ID',
  `user_id`     BIGINT DEFAULT NULL COMMENT '发送者ID（系统消息为NULL）',
  `type`        TINYINT DEFAULT 0 COMMENT '类型：0-文字 1-Emoji 2-系统',
  `content`     VARCHAR(500) DEFAULT NULL COMMENT '内容',
  `seq`         INT    DEFAULT 0 COMMENT '房间内消息序列号（递增，用于去重/补发）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_message_room_seq` (`room_id`, `seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间消息表';
