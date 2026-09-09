package com.itheima.msg.controller;

import com.itheima.domain.common.Result;
import com.itheima.msg.service.SongShareService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 歌曲分享接口
 * <p>
 * 网关路由：/api/share/** → StripPrefix=1 → /share/**
 * <p>
 * 所有接口均需登录，网关解析 JWT 后通过 X-User-Id 头透传用户ID。
 */
@RestController
@RequestMapping("/share")
public class ShareController {

    private final SongShareService songShareService;

    public ShareController(SongShareService songShareService) {
        this.songShareService = songShareService;
    }

    /**
     * 记录分享事件 + 生成分享链接
     * <p>
     * POST /share/music/{musicId}?channel=link
     * channel 可选: link / qrcode / weibo / wechat / copy，默认 link
     */
    @PostMapping("/music/{musicId}")
    public Result<Map<String, Object>> recordShare(
            @PathVariable Integer musicId,
            @RequestHeader("X-User-Id") Integer userId,
            @RequestParam(defaultValue = "link") String channel) {
        Map<String, Object> result = songShareService.recordShare(musicId, userId, channel);
        return Result.success(result);
    }

    /**
     * 查询歌曲分享数
     * <p>
     * GET /share/music/{musicId}/count
     */
    @GetMapping("/music/{musicId}/count")
    public Result<Integer> getShareCount(@PathVariable Integer musicId) {
        int count = songShareService.countByMusicId(musicId);
        return Result.success(count);
    }
}
