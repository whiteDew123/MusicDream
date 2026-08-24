package com.itheima.recognize.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.recognize.algorithm.AudioProcessor;
import com.itheima.recognize.algorithm.FingerprintExtractor;
import com.itheima.recognize.algorithm.FingerprintMatcher;
import com.itheima.recognize.entity.SongFingerprint;
import com.itheima.recognize.mapper.FingerprintMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * 指纹提取与入库服务
 */
@Service
public class FingerprintService {

    private static final int TARGET_SAMPLE_RATE = 8000;

    private final FingerprintMapper fingerprintMapper;

    public FingerprintService(FingerprintMapper fingerprintMapper) {
        this.fingerprintMapper = fingerprintMapper;
    }

    /**
     * 为指定歌曲生成指纹并入库，返回生成的指纹数量
     */
    public int register(File audioFile, Integer musicId) throws Exception {
        List<FingerprintMatcher.HashRecord> records = extractHashRecords(audioFile, musicId);

        // 重新生成前清空旧指纹，避免脏数据累积
        fingerprintMapper.delete(new LambdaQueryWrapper<SongFingerprint>()
                .eq(SongFingerprint::getMusicId, musicId));

        for (FingerprintMatcher.HashRecord record : records) {
            SongFingerprint fp = new SongFingerprint();
            fp.setMusicId(musicId);
            fp.setHashValue(record.hashKey);
            fp.setTimeOffset(record.anchorTime);
            fingerprintMapper.insert(fp);
        }
        return records.size();
    }

    /**
     * 提取录音片段的查询哈希列表（songId 不参与匹配）
     */
    public List<FingerprintMatcher.HashRecord> extractQueryHashes(File audioFile) throws Exception {
        return extractHashRecords(audioFile, 0);
    }

    private List<FingerprintMatcher.HashRecord> extractHashRecords(File audioFile, int songId) throws Exception {
        double[] pcm = AudioProcessor.decodeToPCM(audioFile, TARGET_SAMPLE_RATE);

        FingerprintExtractor extractor = new FingerprintExtractor();
        float[][] spectrogram = extractor.computeSpectrogram(pcm);
        List<FingerprintExtractor.Peak> peaks = extractor.findPeaks(spectrogram);

        FingerprintMatcher matcher = new FingerprintMatcher();
        return matcher.extractFingerprints(peaks, songId, TARGET_SAMPLE_RATE);
    }
}
