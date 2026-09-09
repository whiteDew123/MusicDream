package com.itheima.recognize.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.recognize.algorithm.AudioProcessor;
import com.itheima.recognize.algorithm.FingerprintExtractor;
import com.itheima.recognize.algorithm.FingerprintMatcher;
import com.itheima.recognize.entity.SongFingerprint;
import com.itheima.recognize.mapper.FingerprintMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 指纹提取与入库服务
 */
@Service
public class FingerprintService {

    private static final int TARGET_SAMPLE_RATE = 8000;
    private static final int MAX_FINGERPRINTS_PER_SONG = 20000;
    private static final Logger log = LoggerFactory.getLogger(FingerprintService.class);

    private final FingerprintMapper fingerprintMapper;
    private final JdbcTemplate jdbcTemplate;

    public FingerprintService(FingerprintMapper fingerprintMapper, JdbcTemplate jdbcTemplate) {
        this.fingerprintMapper = fingerprintMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 为指定歌曲生成指纹并入库，返回生成的指纹数量
     */
    public int register(File audioFile, Integer musicId) throws Exception {
        List<FingerprintMatcher.HashRecord> records = extractHashRecords(audioFile, musicId);
        log.info("歌曲指纹提取完成: musicId={}, 哈希数量={}", musicId, records.size());

        if (records.isEmpty()) {
            log.warn("未提取到任何指纹，跳过入库: musicId={}, file={}", musicId, audioFile.getName());
            return 0;
        }

        // 防止单首歌指纹过多导致入库超时，均匀采样截断
        if (records.size() > MAX_FINGERPRINTS_PER_SONG) {
            List<FingerprintMatcher.HashRecord> sampled = new ArrayList<>(MAX_FINGERPRINTS_PER_SONG);
            double step = (double) records.size() / MAX_FINGERPRINTS_PER_SONG;
            for (int i = 0; i < MAX_FINGERPRINTS_PER_SONG; i++) {
                sampled.add(records.get((int) (i * step)));
            }
            records = sampled;
            log.info("指纹数量过大，已采样截断至 {} 条", records.size());
        }

        // 重新生成前清空旧指纹，避免脏数据累积
        fingerprintMapper.delete(new LambdaQueryWrapper<SongFingerprint>()
                .eq(SongFingerprint::getMusicId, musicId));

        // 使用 JDBC 批量插入，避免逐条 insert 导致超时
        List<Object[]> batchArgs = new ArrayList<>(records.size());
        for (FingerprintMatcher.HashRecord record : records) {
            batchArgs.add(new Object[]{musicId, record.hashKey, record.anchorTime});
        }
        jdbcTemplate.batchUpdate(
                "INSERT INTO song_fingerprint (music_id, hash_value, time_offset) VALUES (?, ?, ?)",
                batchArgs
        );

        return records.size();
    }

    /**
     * 分析音频指纹提取情况（调试用，不入库）
     */
    public Map<String, Object> analyze(File audioFile) throws Exception {
        double[] pcm = AudioProcessor.decodeToPCM(audioFile, TARGET_SAMPLE_RATE);
        FingerprintExtractor extractor = new FingerprintExtractor();
        float[][] spectrogram = extractor.computeSpectrogram(pcm);
        List<FingerprintExtractor.Peak> peaks = extractor.findPeaks(spectrogram);
        FingerprintMatcher matcher = new FingerprintMatcher();
        List<FingerprintMatcher.HashRecord> records = matcher.extractFingerprints(peaks, 0, TARGET_SAMPLE_RATE);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pcmLength", pcm.length);
        data.put("frameCount", spectrogram.length);
        data.put("peakCount", peaks.size());
        data.put("hashCount", records.size());
        return data;
    }

    /**
     * 提取录音片段的查询哈希列表（songId 不参与匹配）
     */
    public List<FingerprintMatcher.HashRecord> extractQueryHashes(File audioFile) throws Exception {
        return extractHashRecords(audioFile, 0);
    }

    private List<FingerprintMatcher.HashRecord> extractHashRecords(File audioFile, int songId) throws Exception {
        double[] pcm = AudioProcessor.decodeToPCM(audioFile, TARGET_SAMPLE_RATE);
        log.info("音频解码完成: file={}, pcm长度={}", audioFile.getName(), pcm.length);

        FingerprintExtractor extractor = new FingerprintExtractor();
        float[][] spectrogram = extractor.computeSpectrogram(pcm);
        List<FingerprintExtractor.Peak> peaks = extractor.findPeaks(spectrogram);
        log.info("频谱/峰值提取完成: 帧数={}, 峰值数={}",
                spectrogram.length > 0 ? spectrogram.length : 0, peaks.size());

        FingerprintMatcher matcher = new FingerprintMatcher();
        List<FingerprintMatcher.HashRecord> records = matcher.extractFingerprints(peaks, songId, TARGET_SAMPLE_RATE);
        log.info("指纹配对完成: 哈希数={}", records.size());
        return records;
    }
}