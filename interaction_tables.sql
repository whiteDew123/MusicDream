USE musicdreamer;

/* ========== Table 1/3: song_like (joint PK, idempotent) ========== */
CREATE TABLE IF NOT EXISTS `song_like` (
  `music_id`    int      NOT NULL                COMMENT 'song id',
  `user_id`     int      NOT NULL                COMMENT 'user id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'like time',
  PRIMARY KEY (`music_id`, `user_id`) USING BTREE,
  KEY `idx_user`  (`user_id`)  USING BTREE,
  KEY `idx_music` (`music_id`) USING BTREE,
  CONSTRAINT `fk_songlike_music` FOREIGN KEY (`music_id`) REFERENCES `music` (`music_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_songlike_user`  FOREIGN KEY (`user_id`)  REFERENCES `user`  (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'song like detail' ROW_FORMAT = Dynamic;

/* ========== Table 2/3: song_comment (supports nested replies) ========== */
CREATE TABLE IF NOT EXISTS `song_comment` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT 'comment id',
  `music_id`    int          NOT NULL                COMMENT 'song id',
  `user_id`     int          NOT NULL                COMMENT 'commenter user id',
  `content`     varchar(500) NOT NULL                COMMENT 'comment content',
  `parent_id`   bigint       NULL     DEFAULT NULL   COMMENT 'parent comment id, NULL = top level',
  `to_user_id`  int          NULL     DEFAULT NULL   COMMENT 'reply target user id',
  `likes`       int          NOT NULL DEFAULT 0      COMMENT 'comment like count',
  `is_top`      tinyint      NOT NULL DEFAULT 0      COMMENT 'pinned 0no 1yes',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'comment time',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_music_time` (`music_id`, `create_time` DESC) USING BTREE,
  KEY `idx_user`       (`user_id`)                    USING BTREE,
  KEY `idx_parent`     (`parent_id`)                  USING BTREE,
  CONSTRAINT `fk_comment_music` FOREIGN KEY (`music_id`) REFERENCES `music` (`music_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comment_user`  FOREIGN KEY (`user_id`)  REFERENCES `user`  (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'song comment' ROW_FORMAT = Dynamic;

/* ========== Table 3/3: song_share (for statistics) ========== */
CREATE TABLE IF NOT EXISTS `song_share` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT 'share id',
  `music_id`    int          NOT NULL                COMMENT 'song id',
  `user_id`     int          NOT NULL                COMMENT 'sharer user id',
  `channel`     varchar(16)  NOT NULL DEFAULT 'link' COMMENT 'link/qrcode/weibo/wechat/copy',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'share time',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_music` (`music_id`) USING BTREE,
  KEY `idx_user`  (`user_id`)  USING BTREE,
  CONSTRAINT `fk_share_music` FOREIGN KEY (`music_id`) REFERENCES `music` (`music_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_share_user`  FOREIGN KEY (`user_id`)  REFERENCES `user`  (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'song share record' ROW_FORMAT = Dynamic;

/* ========== Add redundant counter columns to music table ========== */
ALTER TABLE `music`
    ADD COLUMN `likes_count`   int NOT NULL DEFAULT 0 COMMENT 'like count'   AFTER `listen_numb`,
    ADD COLUMN `comment_count` int NOT NULL DEFAULT 0 COMMENT 'comment count' AFTER `likes_count`,
    ADD COLUMN `share_count`   int NOT NULL DEFAULT 0 COMMENT 'share count'   AFTER `comment_count`;
