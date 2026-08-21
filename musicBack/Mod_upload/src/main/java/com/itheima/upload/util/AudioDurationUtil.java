package com.itheima.upload.util;

import com.mpatric.mp3agic.Mp3File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * 音频时长解析工具
 *
 * <p>优先使用 JAudiotagger（支持多格式），失败时用 mp3agic 兜底（MP3 专用）。
 * 两次都失败则返回 null。</p>
 */
public final class AudioDurationUtil {

    private static final Logger log = LoggerFactory.getLogger(AudioDurationUtil.class);

    private AudioDurationUtil() {
    }

    /**
     * 获取音频文件时长（秒）
     *
     * @param filePath 音频文件路径
     * @return 时长（秒）；解析失败返回 null
     */
    public static Integer getDuration(Path filePath) {
        try {
            AudioFile audioFile = AudioFileIO.read(filePath.toFile());
            return audioFile.getAudioHeader().getTrackLength();
        } catch (Exception e) {
            log.debug("jaudiotagger 解析失败，尝试 mp3agic 兜底");
        }

        try {
            Mp3File mp3 = new Mp3File(filePath.toFile());
            return (int) mp3.getLengthInSeconds();
        } catch (Exception e) {
            log.warn("音频时长解析失败: {} - {}", filePath.getFileName(), e.getMessage());
            return null;
        }
    }
}