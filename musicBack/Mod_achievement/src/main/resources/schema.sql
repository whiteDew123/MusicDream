-- =====================================================
-- Mod_achievement 听歌成就模块微服务建表脚本
-- 数据库：musicdreamer（MySQL 8.x）
-- 说明：
--   1) achievement_rule   成就规则静态配置表（预置 9 条）
--   2) user_achievement   用户成就进度/解锁记录表
--   3) user_level         用户等级与积分、打卡 streak 表
-- =====================================================

-- --------------------------------------------------------
-- 1. 成就规则表
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `achievement_rule` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `code`        VARCHAR(64)  NOT NULL COMMENT '规则编码，如 LISTEN_TOTAL_100',
  `name`        VARCHAR(64)  NOT NULL COMMENT '展示名，如 百听不厌',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `type`        VARCHAR(32)  NOT NULL COMMENT '类型：LISTEN_TOTAL/CHECKIN/LIKE_TOTAL',
  `target`      INT          NOT NULL COMMENT '目标阈值',
  `points`      INT          NOT NULL DEFAULT 0 COMMENT '解锁奖励积分',
  `tier`        TINYINT      NOT NULL DEFAULT 1 COMMENT '徽章档位：1铜/2银/3金',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`code`),
  KEY `idx_rule_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就规则表';

-- 预置 9 条规则：累计播放 3 档 + 连续打卡 3 档 + 收藏数量 3 档
INSERT INTO `achievement_rule` (`code`, `name`, `description`, `type`, `target`, `points`, `tier`) VALUES
  ('LISTEN_TOTAL_10',  '初尝音乐',   '累计听满 10 首歌',   'LISTEN_TOTAL', 10,  10,  1),
  ('LISTEN_TOTAL_50',  '渐入佳境',   '累计听满 50 首歌',   'LISTEN_TOTAL', 50,  30,  2),
  ('LISTEN_TOTAL_100', '百听不厌',   '累计听满 100 首歌',  'LISTEN_TOTAL', 100, 60,  3),
  ('CHECKIN_3',        '初心不改',   '连续打卡 3 天',     'CHECKIN',      3,   10,  1),
  ('CHECKIN_7',        '坚持一周',   '连续打卡 7 天',     'CHECKIN',      7,   30,  2),
  ('CHECKIN_30',       '月度坚守',   '连续打卡 30 天',    'CHECKIN',      30,  100, 3),
  ('LIKE_TOTAL_5',     '收藏入门',   '收藏满 5 首歌',     'LIKE_TOTAL',   5,   10,  1),
  ('LIKE_TOTAL_20',    '收藏达人',   '收藏满 20 首歌',    'LIKE_TOTAL',   20,  30,  2),
  ('LIKE_TOTAL_50',    '收藏大师',   '收藏满 50 首歌',    'LIKE_TOTAL',   50,  60,  3)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- --------------------------------------------------------
-- 2. 用户成就进度表
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_achievement` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     INT          NOT NULL COMMENT '用户ID',
  `rule_id`     INT          NOT NULL COMMENT '规则ID',
  `progress`    INT          NOT NULL DEFAULT 0 COMMENT '当前进度',
  `unlocked`    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否解锁：0否/1是',
  `unlock_time` DATETIME     DEFAULT NULL COMMENT '解锁时间',
  `update_time` DATETIME     DEFAULT NULL COMMENT '进度更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_rule` (`user_id`, `rule_id`),
  KEY `idx_user_ach_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就进度表';

-- --------------------------------------------------------
-- 3. 用户等级/积分/打卡表
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_level` (
  `user_id`          INT          NOT NULL COMMENT '用户ID（主键）',
  `points`           INT          NOT NULL DEFAULT 0 COMMENT '累计积分',
  `level`            INT          NOT NULL DEFAULT 1 COMMENT '当前等级',
  `checkin_streak`   INT          NOT NULL DEFAULT 0 COMMENT '连续打卡天数',
  `last_checkin_date` DATE        DEFAULT NULL COMMENT '上次打卡日',
  `update_time`      DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户等级/积分/打卡表';
