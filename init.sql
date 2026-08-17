-- 1. 用户表 (user)
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID，自增',
  `username` varchar(255) DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) DEFAULT NULL COMMENT '密码 (MD5加密)',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱 (唯一)',
  `phone` varchar(255) DEFAULT NULL COMMENT '手机号 (唯一)',
  `role` int(255) DEFAULT '2' COMMENT '角色: 0-管理员, 1-歌手, 2-普通用户',
  `activation` int(255) DEFAULT '0' COMMENT '激活状态: 0-正常, 1-锁定',
  `create_time` date DEFAULT NULL COMMENT '创建时间',
  `image_url` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `about` varchar(255) DEFAULT NULL COMMENT '个人简介',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 音乐表 (music)
CREATE TABLE `music` (
  `music_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '音乐ID，自增',
  `from_singer` int(255) DEFAULT NULL COMMENT '歌手ID',
  `music_name` varchar(255) DEFAULT NULL COMMENT '歌曲名',
  `music_url` varchar(255) DEFAULT NULL COMMENT '音频文件URL',
  `activation` int(255) DEFAULT '0' COMMENT '状态: 0-正常, 1-用户锁定, 2-管理员锁定',
  `listen_numb` int(255) DEFAULT '0' COMMENT '播放量',
  `image_url` varchar(255) DEFAULT NULL COMMENT '封面图片URL',
  `timelength` int(11) DEFAULT NULL COMMENT '时长 (秒)',
  `create_time` date DEFAULT NULL COMMENT '创建时间',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签 (逗号分隔)',
  `lyric` varchar(255) DEFAULT NULL COMMENT '歌词文件URL',
  PRIMARY KEY (`music_id`),
  KEY `fk_singer` (`from_singer`),
  CONSTRAINT `fk_singer` FOREIGN KEY (`from_singer`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐表';

-- 3. 歌单表 (song_list)
CREATE TABLE `song_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '歌单ID，自增',
  `name` varchar(255) DEFAULT NULL COMMENT '歌单名',
  `user` int(255) DEFAULT NULL COMMENT '所属用户ID',
  `image` varchar(255) DEFAULT NULL COMMENT '封面图片URL',
  `message` varchar(255) DEFAULT NULL COMMENT '歌单简介',
  `create_date` date DEFAULT NULL COMMENT '创建时间',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签',
  PRIMARY KEY (`id`),
  KEY `fk_list_user` (`user`),
  CONSTRAINT `fk_list_user` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单表';

-- 4. 歌单-音乐关联表 (list_music)
CREATE TABLE `list_music` (
  `music` int(255) NOT NULL COMMENT '音乐ID',
  `listid` int(255) NOT NULL COMMENT '歌单ID',
  PRIMARY KEY (`music`, `listid`),
  KEY `fk_lm_list` (`listid`),
  CONSTRAINT `fk_lm_music` FOREIGN KEY (`music`) REFERENCES `music` (`music_id`),
  CONSTRAINT `fk_lm_list` FOREIGN KEY (`listid`) REFERENCES `song_list` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单-音乐关联表';

-- 5. 收藏表 (mylike) - 收藏单曲
CREATE TABLE `mylike` (
  `music` int(255) NOT NULL COMMENT '音乐ID',
  `user` int(255) NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`music`, `user`),
  KEY `fk_like_user` (`user`),
  CONSTRAINT `fk_like_music` FOREIGN KEY (`music`) REFERENCES `music` (`music_id`),
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 6. 收藏歌单表 (likelist)
CREATE TABLE `likelist` (
  `userId` int(11) NOT NULL COMMENT '用户ID',
  `listid` int(11) NOT NULL COMMENT '歌单ID',
  PRIMARY KEY (`userId`, `listid`),
  KEY `fk_ll_list` (`listid`),
  CONSTRAINT `fk_ll_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_ll_list` FOREIGN KEY (`listid`) REFERENCES `song_list` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏歌单表';

-- 7. 消息表 (msg)
CREATE TABLE `msg` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '消息ID，自增',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `user_id` int(11) DEFAULT NULL COMMENT '接收用户ID',
  `msg` varchar(255) DEFAULT NULL COMMENT '消息内容',
  `create_time` date DEFAULT NULL COMMENT '创建时间',
  `isread` int(255) DEFAULT '1' COMMENT '是否已读: 0-已读, 1-未读',
  PRIMARY KEY (`id`),
  KEY `fk_msg_user` (`user_id`),
  CONSTRAINT `fk_msg_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 8. 日志表 (log)
CREATE TABLE `log` (
  `userName` varchar(255) DEFAULT NULL COMMENT '操作用户名',
  `do_some` varchar(255) DEFAULT NULL COMMENT '操作内容',
  `MusicName` varchar(255) DEFAULT NULL COMMENT '音乐名称',
  `create_date` date DEFAULT NULL COMMENT '操作日期'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志表';