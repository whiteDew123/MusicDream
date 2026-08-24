package com.itheima.singer.util;

import com.itheima.singer.dto.MusicDTO;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 歌曲自动审核工具
 *
 * <p>当前实现敏感词检查 + URL 合法性检查。
 * 通过则直接上架；不通过则保持待审核状态，由管理员人工兜底审核。</p>
 */
public final class SensitiveWordUtil {

    /** 基础敏感词库（生产环境建议接入正式词库或第三方内容安全服务） */
    private static final List<String> SENSITIVE_WORDS = List.of(
            "赌博", "色情", "暴力", "诈骗", "代开发票", "违禁品", "毒品", "刷单"
    );

    /** URL 必须为 http/https，或是以 /uploads/ 开头的相对路径（上传服务返回格式） */
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://).+", Pattern.CASE_INSENSITIVE);
    /** 上传服务返回的相对路径前缀 */
    private static final String UPLOADS_PREFIX = "/uploads/";

    private SensitiveWordUtil() {
    }

    /**
     * 自动审核
     *
     * @param dto 歌曲信息
     * @return PASS / REJECT(携带原因) / MANUAL 三种结果
     */
    public static ReviewResult autoReview(MusicDTO dto) {
        if (dto == null) {
            return ReviewResult.reject("歌曲信息为空");
        }

        // 1. 敏感词检查：歌名、标签等文本包含违规内容直接驳回
        String sensitive = findSensitive(dto.getMusicName(), dto.getTags());
        if (sensitive != null) {
            return ReviewResult.reject("命中有敏感词：" + sensitive);
        }

        // 2. URL 检查：音频、封面、歌词地址必须是合法 http/https 链接或 /uploads/ 相对路径
        if (!isValidUrl(dto.getMusicUrl())) {
            return ReviewResult.reject("音频URL格式不合法");
        }
        if (!isValidUrl(dto.getImageUrl())) {
            return ReviewResult.reject("封面URL格式不合法");
        }
        if (!isValidUrl(dto.getLyric())) {
            return ReviewResult.reject("歌词URL格式不合法");
        }

        // 3. 信息不完整时无法自动确定，转人工确认
        if (!StringUtils.hasText(dto.getLyric()) || !StringUtils.hasText(dto.getImageUrl())) {
            return ReviewResult.manual();
        }

        // 4. 全部检查通过，自动上架
        return ReviewResult.pass();
    }

    /**
     * 检查文本中是否包含敏感词，返回命中的敏感词；没有则返回 null
     */
    private static String findSensitive(String... texts) {
        for (String text : texts) {
            if (!StringUtils.hasText(text)) {
                continue;
            }
            for (String word : SENSITIVE_WORDS) {
                if (text.contains(word)) {
                    return word;
                }
            }
        }
        return null;
    }

    /**
     * URL 合法检查：允许为空、http/https 地址、或 /uploads/ 开头的相对路径
     */
    private static boolean isValidUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return true;
        }
        String trimmed = url.trim();
        // 上传服务返回的相对路径（如 /uploads/music/xxx.mp3）
        if (trimmed.startsWith(UPLOADS_PREFIX)) {
            return true;
        }
        return URL_PATTERN.matcher(trimmed).matches();
    }
}