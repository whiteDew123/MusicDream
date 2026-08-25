/*
 Navicat Premium Data Transfer

 Source Server         : musicdream
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : musicdreamer

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 25/08/2026 11:01:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for capsule
-- ----------------------------
DROP TABLE IF EXISTS `capsule`;
CREATE TABLE `capsule`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `sender_id` int NOT NULL COMMENT '发送者 user.id',
  `receiver_id` int NOT NULL DEFAULT 0 COMMENT '接收者 user.id，0=给自己',
  `music_id` int NOT NULL COMMENT '关联 music.music_id',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '留言内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `unlock_time` datetime NOT NULL COMMENT '解锁时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0-封印 1-已解锁 2-已公开',
  `is_public` tinyint NOT NULL DEFAULT 0 COMMENT '0-私密 1-公开到广场',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_unlock`(`status` ASC, `unlock_time` ASC) USING BTREE,
  INDEX `idx_sender`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver`(`receiver_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '音乐时空胶囊' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for capsule_like
-- ----------------------------
DROP TABLE IF EXISTS `capsule_like`;
CREATE TABLE `capsule_like`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `capsule_id` int NOT NULL COMMENT '胶囊 ID',
  `user_id` int NOT NULL COMMENT '点赞者 user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_capsule_user`(`capsule_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_capsule`(`capsule_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '胶囊点赞' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for like_music
-- ----------------------------
DROP TABLE IF EXISTS `like_music`;
CREATE TABLE `like_music`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `music_id` int NOT NULL COMMENT '音乐ID',
  `create_date` date NULL DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_like_music_user`(`user_id` ASC) USING BTREE,
  INDEX `fk_like_music_music`(`music_id` ASC) USING BTREE,
  CONSTRAINT `fk_like_music_music` FOREIGN KEY (`music_id`) REFERENCES `music` (`music_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_like_music_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏歌曲表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for likelist
-- ----------------------------
DROP TABLE IF EXISTS `likelist`;
CREATE TABLE `likelist`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `list_id` int NOT NULL COMMENT '歌单ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_list`(`user_id` ASC, `list_id` ASC) USING BTREE,
  INDEX `fk_ll_list`(`list_id` ASC) USING BTREE,
  CONSTRAINT `fk_ll_list` FOREIGN KEY (`list_id`) REFERENCES `song_list` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ll_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏歌单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for list_music
-- ----------------------------
DROP TABLE IF EXISTS `list_music`;
CREATE TABLE `list_music`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `music_id` int NOT NULL COMMENT '音乐ID',
  `list_id` int NOT NULL COMMENT '歌单ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_music_list`(`music_id` ASC, `list_id` ASC) USING BTREE,
  INDEX `fk_lm_list`(`list_id` ASC) USING BTREE,
  CONSTRAINT `fk_lm_list` FOREIGN KEY (`list_id`) REFERENCES `song_list` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_lm_music` FOREIGN KEY (`music_id`) REFERENCES `music` (`music_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '歌单-歌曲关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for log
-- ----------------------------
DROP TABLE IF EXISTS `log`;
CREATE TABLE `log`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `userName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户名',
  `do_some` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作内容',
  `MusicName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '音乐名称',
  `create_date` date NULL DEFAULT NULL COMMENT '操作日期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for msg
-- ----------------------------
DROP TABLE IF EXISTS `msg`;
CREATE TABLE `msg`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `user_id` int NULL DEFAULT NULL COMMENT '接收用户ID',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息内容',
  `create_time` date NULL DEFAULT NULL COMMENT '创建时间',
  `isread` int NULL DEFAULT 1 COMMENT '是否已读：0-已读 1-未读',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_msg_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_msg_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for music
-- ----------------------------
DROP TABLE IF EXISTS `music`;
CREATE TABLE `music`  (
  `music_id` int NOT NULL AUTO_INCREMENT COMMENT '音乐ID',
  `from_singer` int NULL DEFAULT NULL COMMENT '歌手ID',
  `music_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '歌曲名',
  `music_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '音频文件URL',
  `activation` int NULL DEFAULT 0 COMMENT '状态：0-正常 1-用户锁定 2-管理员锁定',
  `listen_numb` int NULL DEFAULT 0 COMMENT '播放量',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `timelength` int NULL DEFAULT NULL COMMENT '时长（秒）',
  `create_time` date NULL DEFAULT NULL COMMENT '创建时间',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签（逗号分隔）',
  `lyric` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '歌词文件URL',
  `audit_status` int NULL DEFAULT 0 COMMENT '审核状态：0-待审核 1-已通过 2-已驳回',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  PRIMARY KEY (`music_id`) USING BTREE,
  INDEX `fk_singer`(`from_singer` ASC) USING BTREE,
  CONSTRAINT `fk_singer` FOREIGN KEY (`from_singer`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '音乐表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for song_fingerprint
-- ----------------------------
DROP TABLE IF EXISTS `song_fingerprint`;
CREATE TABLE `song_fingerprint`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '指纹ID',
  `music_id` int NOT NULL COMMENT '歌曲ID，关联 music.music_id',
  `hash_value` bigint NOT NULL COMMENT '指纹哈希值',
  `time_offset` double NOT NULL COMMENT '该指纹在歌曲中的时间偏移（秒）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_hash`(`hash_value` ASC) USING BTREE,
  INDEX `idx_music`(`music_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '歌曲指纹表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for song_list
-- ----------------------------
DROP TABLE IF EXISTS `song_list`;
CREATE TABLE `song_list`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '歌单ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '歌单名',
  `user_id` int NULL DEFAULT NULL COMMENT '所属用户ID',
  `pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `introduction` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '歌单简介',
  `create_date` date NULL DEFAULT NULL COMMENT '创建时间',
  `style` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风格',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_list_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_list_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '歌单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码（MD5加密）',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `role` int NULL DEFAULT 2 COMMENT '角色：0-管理员 1-歌手 2-普通用户',
  `activation` int NULL DEFAULT 0 COMMENT '激活状态：0-正常 1-锁定',
  `create_time` date NULL DEFAULT NULL COMMENT '创建时间',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像地址',
  `about` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
