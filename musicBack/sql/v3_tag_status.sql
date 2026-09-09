-- ============================================================
-- 歌曲标签化重构 · 第三期 SQL（标签启用/禁用）
-- 前置：已执行 v2_tag_schema.sql（tag / music_tag 表就绪）
-- 说明：为标签字典增加状态位；存量 23 个预设标签自动默认 1（启用），
--       禁用仅影响“新选择”，不影响已有 music_tag 关联匹配。
-- 注意：MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，本脚本一次性执行。
-- ============================================================

ALTER TABLE `tag`
  ADD COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用' AFTER `name`;