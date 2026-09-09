-- ============================================================
-- 歌曲标签化重构 · 第二期 SQL（建表 + 预设标签）
-- 执行顺序：先执行本文件，再执行 v2_tag_migrate.sql（存量迁移）
-- 说明：music.tags 字段保留作为冗余，三期消费方切到 music_tag 后下线
-- ============================================================

-- 1. 标签字典表
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `tag_id` int NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类别码：genre-风格 mood-情绪（预留 lang/era/scene/inst）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`tag_id`) USING BTREE,
  UNIQUE INDEX `uk_code_name` (`code`, `name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签字典表' ROW_FORMAT = Dynamic;

-- 2. 歌曲-标签关联表（联合唯一索引防止重复打标）
DROP TABLE IF EXISTS `music_tag`;
CREATE TABLE `music_tag` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `music_id` int NOT NULL COMMENT '歌曲ID（music.music_id）',
  `tag_id` int NOT NULL COMMENT '标签ID（tag.tag_id）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_music_tag` (`music_id`, `tag_id`) USING BTREE,
  INDEX `idx_tag_id` (`tag_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '歌曲-标签关联表' ROW_FORMAT = Dynamic;

-- 3. 预设标签写库（第一期 23 个：风格 15 + 情绪 8）
INSERT INTO `tag` (`code`, `name`) VALUES
('genre', '流行'), ('genre', '摇滚'), ('genre', '电子'), ('genre', '民谣'), ('genre', 'R&B'),
('genre', '嘻哈'), ('genre', '爵士'), ('genre', '古典'), ('genre', '轻音乐'), ('genre', '古风'),
('genre', '国风'), ('genre', '说唱'), ('genre', '雷鬼'), ('genre', '灵魂乐'), ('genre', '另类/独立'),
('mood', '欢快'), ('mood', '悲伤'), ('mood', '治愈'), ('mood', '激情'),
('mood', '慵懒'), ('mood', '伤感'), ('mood', '甜蜜'), ('mood', '励志');