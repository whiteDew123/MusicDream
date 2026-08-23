-- ============================================
-- MusicDreamer 数据库重建脚本
-- 执行前请先删除旧表（会清空所有数据）
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `log`;
DROP TABLE IF EXISTS `mylike`;
DROP TABLE IF EXISTS `likelist`;
DROP TABLE IF EXISTS `msg`;
DROP TABLE IF EXISTS `list_music`;
DROP TABLE IF EXISTS `song_list`;
DROP TABLE IF EXISTS `music`;
DROP TABLE IF EXISTS `user`;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(255) DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) DEFAULT NULL COMMENT '密码(MD5加密)',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(255) DEFAULT NULL COMMENT '手机号',
  `role` int DEFAULT 2 COMMENT '角色: 0-管理员 1-歌手 2-普通用户',
  `activation` int DEFAULT 0 COMMENT '状态: 0-正常 1-锁定',
  `create_time` date DEFAULT NULL COMMENT '创建时间',
  `image_url` varchar(255) DEFAULT NULL COMMENT '头像',
  `about` varchar(255) DEFAULT NULL COMMENT '个人简介',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 音乐表
-- ============================================
CREATE TABLE `music` (
  `music_id` int NOT NULL AUTO_INCREMENT COMMENT '音乐ID',
  `from_singer` int DEFAULT NULL COMMENT '歌手ID',
  `music_name` varchar(255) DEFAULT NULL COMMENT '歌曲名',
  `music_url` varchar(255) DEFAULT NULL COMMENT '音频URL',
  `activation` int DEFAULT 0 COMMENT '状态: 0-正常 1-用户锁定 2-管理员锁定',
  `listen_numb` int DEFAULT 0 COMMENT '播放量',
  `image_url` varchar(255) DEFAULT NULL COMMENT '封面',
  `timelength` int DEFAULT NULL COMMENT '时长(秒)',
  `create_time` date DEFAULT NULL COMMENT '创建时间',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签',
  `lyric` varchar(255) DEFAULT NULL COMMENT '歌词URL',
  PRIMARY KEY (`music_id`),
  KEY `fk_singer` (`from_singer`),
  CONSTRAINT `fk_singer` FOREIGN KEY (`from_singer`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐表';

-- ============================================
-- 3. 歌单表
-- ============================================
CREATE TABLE `song_list` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '歌单ID',
  `name` varchar(255) DEFAULT NULL COMMENT '歌单名',
  `user` int DEFAULT NULL COMMENT '所属用户ID',
  `image` varchar(255) DEFAULT NULL COMMENT '封面',
  `message` varchar(255) DEFAULT NULL COMMENT '简介',
  `create_date` date DEFAULT NULL COMMENT '创建时间',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签',
  PRIMARY KEY (`id`),
  KEY `fk_list_user` (`user`),
  CONSTRAINT `fk_list_user` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单表';

-- ============================================
-- 4. 歌单-音乐关联表
-- ============================================
CREATE TABLE `list_music` (
  `music` int NOT NULL COMMENT '音乐ID',
  `listid` int NOT NULL COMMENT '歌单ID',
  PRIMARY KEY (`music`, `listid`),
  KEY `fk_lm_list` (`listid`),
  CONSTRAINT `fk_lm_music` FOREIGN KEY (`music`) REFERENCES `music` (`music_id`),
  CONSTRAINT `fk_lm_list` FOREIGN KEY (`listid`) REFERENCES `song_list` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单音乐关联表';

-- ============================================
-- 5. 收藏表(单曲)
-- ============================================
CREATE TABLE `mylike` (
  `music` int NOT NULL COMMENT '音乐ID',
  `user` int NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`music`, `user`),
  KEY `fk_like_user` (`user`),
  CONSTRAINT `fk_like_music` FOREIGN KEY (`music`) REFERENCES `music` (`music_id`),
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ============================================
-- 6. 收藏歌单表
-- ============================================
CREATE TABLE `likelist` (
  `userId` int NOT NULL COMMENT '用户ID',
  `listid` int NOT NULL COMMENT '歌单ID',
  PRIMARY KEY (`userId`, `listid`),
  KEY `fk_ll_list` (`listid`),
  CONSTRAINT `fk_ll_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_ll_list` FOREIGN KEY (`listid`) REFERENCES `song_list` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏歌单表';

-- ============================================
-- 7. 消息表
-- ============================================
CREATE TABLE `msg` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `user_id` int DEFAULT NULL COMMENT '接收用户ID',
  `msg` varchar(255) DEFAULT NULL COMMENT '消息内容',
  `create_time` date DEFAULT NULL COMMENT '创建时间',
  `isread` int DEFAULT 1 COMMENT '已读: 0-已读 1-未读',
  PRIMARY KEY (`id`),
  KEY `fk_msg_user` (`user_id`),
  CONSTRAINT `fk_msg_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ============================================
-- 8. 日志表
-- ============================================
CREATE TABLE `log` (
  `log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `userName` varchar(255) DEFAULT NULL COMMENT '操作用户名',
  `do_some` varchar(255) DEFAULT NULL COMMENT '操作内容',
  `MusicName` varchar(255) DEFAULT NULL COMMENT '音乐名称',
  `create_date` date DEFAULT NULL COMMENT '操作日期',
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志表';

-- ============================================
-- 测试数据
-- ============================================

-- 用户数据 (密码: 123456 的 MD5 = e10adc3949ba59abbe56e057f)
INSERT INTO `user` (`id`, `username`, `password`, `email`, `phone`, `role`, `activation`, `create_time`, `image_url`) VALUES
(1, 'admin',    'e10adc3949ba59abbe56e057f', 'admin@test.com',    '13800000000', 0, 0, CURDATE(), NULL),
(2, '张三',     'e10adc3949ba59abbe56e057f', 'zhangsan@test.com', '13800000001', 2, 0, CURDATE(), NULL),
(3, '李四',     'e10adc3949ba59abbe56e057f', 'lisi@test.com',     '13800000002', 1, 0, CURDATE(), NULL),
(4, '王五',     'e10adc3949ba59abbe56e057f', 'wangwu@test.com',   '13800000003', 2, 0, CURDATE(), NULL),
(11, 'hss',     'e10adc3949ba59abbe56e057f', 'hss@test.com',      '13800000011', 2, 0, CURDATE(), NULL);

-- 音乐数据
INSERT INTO `music` (`music_id`, `from_singer`, `music_name`, `music_url`, `activation`, `listen_numb`, `image_url`, `timelength`, `create_time`, `tags`) VALUES
(1, 3, '稻香',     '/music/daoxiang.mp3',     0, 2000, '/img/daoxiang.jpg',     223, CURDATE(), '民谣'),
(2, 3, '告白气球', '/music/gaobai.mp3',      0, 3000, '/img/gaobai.jpg',       215, CURDATE(), '流行'),
(3, 2, '夜曲',     '/music/yequ.mp3',         0, 1000, '/img/yequ.jpg',         245, CURDATE(), '流行'),
(4, 11, '测试歌曲1', '/music/test1.mp3',      0, 100,  '/img/test1.jpg',        245, CURDATE(), '流行'),
(5, 11, '测试歌曲2', '/music/test2.mp3',      0, 200,  '/img/test2.jpg',        223, CURDATE(), '民谣');

-- 日志数据
INSERT INTO `log` (`userName`, `do_some`, `MusicName`, `create_date`) VALUES
('admin', '冻结用户', '夜曲', CURDATE()),
('admin', '删除歌曲', '稻香', CURDATE()),
('hss',   '上传歌曲', '测试歌曲1', CURDATE()),
('admin', '解冻用户', NULL, CURDATE());

SET FOREIGN_KEY_CHECKS = 1;
