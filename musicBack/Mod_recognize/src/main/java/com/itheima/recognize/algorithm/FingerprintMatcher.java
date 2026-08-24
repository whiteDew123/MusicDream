package com.itheima.recognize.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 指纹哈希配对生成
 */
public class FingerprintMatcher {

    private static final int MIN_DELTA_FRAME = 3;
    private static final int MAX_DELTA_FRAME = 10;
    private static final int MAX_FREQ_DELTA = 2000;

    /**
     * 从峰值列表生成指纹哈希记录
     */
    public List<HashRecord> extractFingerprints(List<FingerprintExtractor.Peak> peaks, int songId, int sampleRate) {
        List<HashRecord> records = new ArrayList<>();
        for (int i = 0; i < peaks.size(); i++) {
            FingerprintExtractor.Peak anchor = peaks.get(i);
            for (int j = i + 1; j < peaks.size(); j++) {
                FingerprintExtractor.Peak target = peaks.get(j);
                int deltaFrame = target.frame - anchor.frame;
                if (deltaFrame < MIN_DELTA_FRAME || deltaFrame > MAX_DELTA_FRAME) {
                    continue;
                }

                int deltaFreq = Math.abs(target.freq - anchor.freq);
                if (deltaFreq > MAX_FREQ_DELTA) {
                    continue;
                }

                int anchorFreqHz = anchor.freq * sampleRate / FingerprintExtractor.FFT_SIZE;
                int targetFreqHz = target.freq * sampleRate / FingerprintExtractor.FFT_SIZE;

                long hashKey = ((long) anchorFreqHz << 32)
                        | ((long) targetFreqHz << 16)
                        | deltaFrame;

                double anchorTime = (double) anchor.frame * FingerprintExtractor.HOP_SIZE / sampleRate;
                records.add(new HashRecord(hashKey, songId, anchorTime));
            }
        }
        return records;
    }

    public static class HashRecord {
        public final long hashKey;
        public final int songId;
        public final double anchorTime;

        public HashRecord(long hashKey, int songId, double anchorTime) {
            this.hashKey = hashKey;
            this.songId = songId;
            this.anchorTime = anchorTime;
        }
    }
}
