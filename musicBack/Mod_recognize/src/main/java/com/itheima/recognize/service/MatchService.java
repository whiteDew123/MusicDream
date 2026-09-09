package com.itheima.recognize.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.recognize.algorithm.FingerprintMatcher;
import com.itheima.recognize.dto.RecognizeResult;
import com.itheima.recognize.entity.SongFingerprint;
import com.itheima.recognize.mapper.FingerprintMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 听歌识曲匹配服务
 *
 * <p>使用 Shazam 式投票：同一首歌的所有指纹，偏移量应当集中在一个值附近。</p>
 */
@Service
public class MatchService {

    /** 最低命中票数，低于该值视为未识别 */
    private static final int MIN_VOTES = 3;

    private final FingerprintService fingerprintService;
    private final FingerprintMapper fingerprintMapper;

    public MatchService(FingerprintService fingerprintService, FingerprintMapper fingerprintMapper) {
        this.fingerprintService = fingerprintService;
        this.fingerprintMapper = fingerprintMapper;
    }

    /**
     * 匹配录音文件，返回识别结果；未识别返回 success=false
     */
    public RecognizeResult match(File audioFile) throws Exception {
        List<FingerprintMatcher.HashRecord> queryHashes = fingerprintService.extractQueryHashes(audioFile);

        // musicId -> (roundedOffset -> votes)
        Map<Integer, Map<Double, Integer>> votes = new HashMap<>();

        for (FingerprintMatcher.HashRecord query : queryHashes) {
            List<SongFingerprint> candidates = fingerprintMapper.selectList(
                    new LambdaQueryWrapper<SongFingerprint>()
                            .eq(SongFingerprint::getHashValue, query.hashKey));

            for (SongFingerprint candidate : candidates) {
                double offset = candidate.getTimeOffset() - query.anchorTime;
                double roundedOffset = Math.round(offset * 10.0) / 10.0;

                Map<Double, Integer> songVotes = votes.computeIfAbsent(
                        candidate.getMusicId(), k -> new HashMap<>());
                songVotes.put(roundedOffset, songVotes.getOrDefault(roundedOffset, 0) + 1);
            }
        }

        int bestMusicId = -1;
        int bestVoteCount = 0;

        for (Map.Entry<Integer, Map<Double, Integer>> songEntry : votes.entrySet()) {
            int maxVote = songEntry.getValue().values().stream()
                    .max(Integer::compareTo)
                    .orElse(0);
            if (maxVote > bestVoteCount) {
                bestVoteCount = maxVote;
                bestMusicId = songEntry.getKey();
            }
        }

        RecognizeResult result = new RecognizeResult();
        result.setSuccess(bestVoteCount >= MIN_VOTES && bestMusicId > 0);
        result.setMusicId(result.getSuccess() ? bestMusicId : null);
        result.setMatchScore(result.getSuccess() ? bestVoteCount : 0);
        return result;
    }
}
