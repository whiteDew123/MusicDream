package com.itheima.upload.util;

import com.mpatric.mp3agic.Mp3File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * 音频时长解析工具（三层兜底）
 *
 * <p>1. ffprobe — 外部进程，最准确，优先使用</p>
 * <p>2. JAudiotagger — 纯 Java，覆盖 MP3/FLAC/WAV/OGG/MP4，毫秒级</p>
 * <p>3. mp3agic — MP3 专用纯 Java 兜底</p>
 */
public final class AudioDurationUtil {

    private static final Logger log = LoggerFactory.getLogger(AudioDurationUtil.class);

    private static String ffprobePath = "ffprobe";

    private AudioDurationUtil() {
    }

    /**
     * 设置 ffprobe 路径（由 Spring 配置注入）
     */
    public static void setFfprobePath(String path) {
        if (path != null && !path.isBlank()) {
            ffprobePath = path;
        }
    }

    /**
     * 获取音频文件时长（秒）
     *
     * @param filePath 音频文件路径
     * @return 时长（秒）；解析失败返回 null
     */
    public static Integer getDuration(Path filePath) {
        // 1. ffprobe（最准确，优先使用）
        Integer duration = parseWithFfprobe(filePath);
        if (duration != null) {
            return duration;
        }

        // 2. JAudiotagger（纯 Java，覆盖主流格式）
        try {
            AudioFile audioFile = AudioFileIO.read(filePath.toFile());
            int sec = audioFile.getAudioHeader().getTrackLength();
            if (sec > 0) {
                return sec;
            }
        } catch (Exception ignored) {
            // 非致命，继续下一个
        }

        // 3. mp3agic（MP3 专用纯 Java 兜底）
        try {
            Mp3File mp3 = new Mp3File(filePath.toFile());
            long seconds = mp3.getLengthInSeconds();
            if (seconds > 0) {
                return (int) seconds;
            }
        } catch (Exception ignored) {
            // 非致命，继续下一个
        }

        log.warn("所有时长解析方式均失败: {}", filePath.getFileName());
        return null;
    }

    private static Integer parseWithFfprobe(Path filePath) {
        try {
            Process process = new ProcessBuilder(
                    ffprobePath,
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    filePath.toAbsolutePath().toString()
            ).start();

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            int exitCode = process.waitFor();

            if (exitCode == 0 && !output.isEmpty()) {
                double seconds = Double.parseDouble(output);
                if (seconds > 0) {
                    log.info("ffprobe 解析成功: {} -> {} 秒", filePath.getFileName(), seconds);
                    return (int) Math.round(seconds);
                }
            }

            String errOutput = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            log.warn("ffprobe 解析失败 (exit={}): {}", exitCode, errOutput);
        } catch (Exception e) {
            log.warn("ffprobe 调用异常: {}", e.getMessage());
        }

        return null;
    }
}