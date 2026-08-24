package com.itheima.capsule.scheduler;

import com.itheima.capsule.mapper.CapsuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：扫描到期胶囊自动解锁
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CapsuleScheduler {

    private final CapsuleMapper capsuleMapper;

    /**
     * 每分钟扫描一次到期胶囊
     */
    @Scheduled(fixedRate = 60000)
    public void unlockExpiredCapsules() {
        int count = capsuleMapper.unlockExpiredCapsules();
        if (count > 0) {
            log.info("自动解锁 {} 个到期胶囊", count);
        }
    }
}
