-- ============================================================
-- 歌曲标签化重构 · 第二期 SQL（存量迁移，一次性执行）
-- 前置：已执行 v2_tag_schema.sql（tag / music_tag 表与预设标签就绪）
--
-- 迁移规则：
--   1. 拆分 music.tags（逗号分隔）逐个处理；
--   2. 带前缀 "code:name" → 按 (code, name) 匹配字典；
--   3. 无前缀 "name"     → 按 name 匹配字典（可正确归入 genre/mood）；
--   4. 命中字典 → INSERT IGNORE 写入 music_tag；
--   5. 未命中（孤儿标签，如"复古""经典""悲情"）→ 记入临时清单表，
--      执行完毕后输出清单，人工在管理端「编辑歌曲」弹窗用新选择器重新打标即可。
-- ============================================================

-- 1. 建临时孤儿清单表
DROP TEMPORARY TABLE IF EXISTS tmp_tag_orphan;
CREATE TEMPORARY TABLE tmp_tag_orphan (
  `music_id` int NOT NULL COMMENT '歌曲ID',
  `token` varchar(100) NOT NULL COMMENT '未命中的原始标签'
);

-- 2. 迁移存储过程
DELIMITER $$
DROP PROCEDURE IF EXISTS migrate_music_tags$$
CREATE PROCEDURE migrate_music_tags()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE m_id INT;
  DECLARE m_tags VARCHAR(255);
  DECLARE token VARCHAR(100);
  DECLARE rest VARCHAR(255);
  DECLARE var_code VARCHAR(20);
  DECLARE var_name VARCHAR(50);
  DECLARE t_id INT;
  DECLARE cur CURSOR FOR
    SELECT music_id, tags FROM music WHERE tags IS NOT NULL AND tags <> '';
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO m_id, m_tags;
    IF done = 1 THEN
      LEAVE read_loop;
    END IF;

    SET rest = m_tags;
    WHILE rest IS NOT NULL AND rest <> '' DO
      SET token = TRIM(SUBSTRING_INDEX(rest, ',', 1));
      IF LOCATE(',', rest) > 0 THEN
        SET rest = TRIM(SUBSTRING(rest, LOCATE(',', rest) + 1));
      ELSE
        SET rest = NULL;
      END IF;

      IF token <> '' THEN
        IF LOCATE(':', token) > 0 THEN
          SET var_code = TRIM(SUBSTRING_INDEX(token, ':', 1));
          SET var_name = TRIM(SUBSTRING_INDEX(token, ':', -1));
          SET t_id = (SELECT tag_id FROM tag WHERE code = var_code AND name = var_name LIMIT 1);
        ELSE
          SET var_code = NULL;
          SET var_name = token;
          SET t_id = (SELECT tag_id FROM tag WHERE name = var_name LIMIT 1);
        END IF;

        IF t_id IS NOT NULL THEN
          INSERT IGNORE INTO music_tag (music_id, tag_id) VALUES (m_id, t_id);
        ELSE
          INSERT INTO tmp_tag_orphan (music_id, token) VALUES (m_id, token);
        END IF;
      END IF;
    END WHILE;
  END LOOP;
  CLOSE cur;
END$$
DELIMITER ;

-- 3. 执行迁移
CALL migrate_music_tags();

-- 4. 输出孤儿清单（人工在管理端重新打标处理）
SELECT * FROM tmp_tag_orphan;

-- 5. 迁移结果核对（歌曲 + 命中的标签映射）
SELECT mt.music_id, m.music_name, t.code, t.name
FROM music_tag mt
JOIN tag t ON t.tag_id = mt.tag_id
JOIN music m ON m.music_id = mt.music_id
ORDER BY mt.music_id, t.code, t.name;

-- 6. 清理
DROP PROCEDURE IF EXISTS migrate_music_tags;
DROP TEMPORARY TABLE IF EXISTS tmp_tag_orphan;