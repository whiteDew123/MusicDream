package com.itheima.msg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.msg.entity.SongShare;
import com.itheima.msg.mapper.SongShareMapper;
import com.itheima.msg.service.SongShareService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class SongShareServiceImpl extends ServiceImpl<SongShareMapper, SongShare> implements SongShareService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> recordShare(Integer musicId, Integer userId, String channel) {
        Map<String, Object> result = new HashMap<>();

        // 去重：同一用户同渠道当天是否已分享过
        Date[] todayRange = todayRange();
        Long todayCount = baseMapper.selectCount(new LambdaQueryWrapper<SongShare>()
                .eq(SongShare::getMusicId, musicId)
                .eq(SongShare::getUserId, userId)
                .eq(SongShare::getChannel, channel)
                .between(SongShare::getCreateTime, todayRange[0], todayRange[1]));

        // 无论是否去重，share_count 都 +1（前端按钮显示照加）
        baseMapper.incrShareCount(musicId);

        // 仅当天首次记录才写入 song_share（避免刷量表膨胀）
        if (todayCount == null || todayCount == 0) {
            SongShare share = new SongShare();
            share.setMusicId(musicId);
            share.setUserId(userId);
            share.setChannel(channel != null ? channel : "link");
            share.setCreateTime(new Date());
            baseMapper.insert(share);
        }

        // 生成分享落地页 URL（前端路由 /share/music/{musicId}）
        String shareUrl = "/share/music/" + musicId;
        result.put("shareUrl", shareUrl);
        result.put("shareCount", baseMapper.selectShareCount(musicId));
        return result;
    }

    @Override
    public int countByMusicId(Integer musicId) {
        return baseMapper.selectShareCount(musicId);
    }

    /**
     * 获取今天 00:00:00 ~ 23:59:59 的时间范围
     */
    private Date[] todayRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date end = cal.getTime();
        return new Date[]{start, end};
    }
}
