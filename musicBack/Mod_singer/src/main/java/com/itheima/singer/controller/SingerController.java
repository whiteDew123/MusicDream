package com.itheima.singer.controller;

import com.itheima.domain.common.PageResult;
import com.itheima.domain.common.Result;
import com.itheima.singer.dto.MusicDTO;
import com.itheima.singer.service.SingerService;
import com.itheima.singer.vo.MusicVO;
import com.itheima.singer.vo.SingerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 歌手模块接口
 */
@RestController
@RequestMapping("/singer")
@RequiredArgsConstructor
public class SingerController {

    private final SingerService singerService;

    /**
     * 分页查询歌曲（管理端/歌手端通用，支持关键词和状态筛选）
     */
    @GetMapping("/songs")
    public Result<PageResult<MusicVO>> pageSongs(@RequestParam(required = false) Integer singerId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Integer activation,
                                                 @RequestParam(required = false) Integer auditStatus) {
        return Result.success(singerService.pageSongs(singerId, page, size, keyword, activation, auditStatus));
    }

    /**
     * 发布歌曲（公开接口，用于用户端APP）
     */
    @PostMapping("/songs")
    public Result<MusicVO> publishSong(@RequestBody MusicDTO dto) {
        if (dto.getFromSinger() == null) {
            return Result.error(400, "歌手ID不能为空");
        }
        if (dto.getMusicName() == null || dto.getMusicName().trim().isEmpty()) {
            return Result.error(400, "歌曲名不能为空");
        }
        return Result.success("发布成功", singerService.publishSong(dto));
    }

    /**
     * 管理端发布歌曲（歌手发布歌曲专用）
     * 从网关透传的请求头中获取当前登录用户信息，防止伪造 fromSinger
     */
    @PostMapping("/addMusic")
    public Result<MusicVO> addMusic(@RequestBody MusicDTO dto,
                                    @RequestHeader("X-User-Id") String userIdHeader,
                                    @RequestHeader("X-Role") String roleHeader) {
        Integer userId;
        try {
            userId = Integer.parseInt(userIdHeader);
        } catch (NumberFormatException e) {
            return Result.error(401, "用户身份无效");
        }

        if (!"1".equals(roleHeader)) {
            return Result.error(403, "仅歌手角色可以发布歌曲");
        }

        dto.setFromSinger(userId);

        MusicVO vo = singerService.addMusic(dto);
        return Result.success("发布成功", vo);
    }

    /**
     * 修改歌曲信息
     */
    @PutMapping("/songs/{musicId}")
    public Result<MusicVO> updateSong(@PathVariable Integer musicId,
                                       @RequestBody MusicDTO dto,
                                       @RequestHeader("X-Role") String roleHeader) {
        if (!"0".equals(roleHeader)) {
            return Result.error(403, "仅管理员可以修改歌曲信息");
        }
        MusicVO updated = singerService.updateSong(musicId, dto);
        return updated == null ? Result.error(404, "歌曲不存在") : Result.success("修改成功", updated);
    }

    /**
     * 更新歌曲状态（管理员锁定/解锁）
     */
    @PutMapping("/songs/{musicId}/status")
    public Result<MusicVO> updateMusicStatus(@PathVariable Integer musicId,
                                              @RequestBody Map<String, Integer> body,
                                              @RequestHeader("X-Role") String roleHeader) {
        if (!"0".equals(roleHeader)) {
            return Result.error(403, "仅管理员可以修改歌曲状态");
        }

        Integer activation = body.get("activation");
        if (activation == null) {
            return Result.error(400, "状态参数不能为空");
        }
        MusicVO updated = singerService.updateMusicStatus(musicId, activation);
        return updated == null ? Result.error(404, "歌曲不存在") : Result.success("状态更新成功", updated);
    }

    /**
     * 删除歌曲（软删除，将 activation 置为 1）
     */
    @DeleteMapping("/songs/{musicId}")
    public Result<Void> deleteSong(@PathVariable Integer musicId) {
        boolean deleted = singerService.deleteSong(musicId);
        return deleted ? Result.<Void>success("删除成功", null) : Result.<Void>error(404, "歌曲不存在");
    }

    /**
     * 获取歌手数据
     */
    @GetMapping("/info/{singerId}")
    public Result<SingerVO> singerInfo(@PathVariable Integer singerId) {
        SingerVO singer = singerService.getSingerInfo(singerId);
        return singer == null ? Result.error(404, "歌手不存在") : Result.success(singer);
    }

    /**
     * 审核歌曲（管理员权限）
     */
    @PutMapping("/songs/{musicId}/audit")
    public Result<MusicVO> auditSong(@PathVariable Integer musicId,
                                     @RequestBody Map<String, Object> body,
                                     @RequestHeader("X-Role") String roleHeader) {
        if (!"0".equals(roleHeader)) {
            return Result.error(403, "仅管理员可以审核歌曲");
        }

        Integer auditStatus = body.get("auditStatus") != null
                ? ((Number) body.get("auditStatus")).intValue()
                : null;
        String auditRemark = body.get("auditRemark") != null
                ? body.get("auditRemark").toString()
                : null;

        if (auditStatus == null || (auditStatus != 1 && auditStatus != 2)) {
            return Result.error(400, "审核状态参数错误（1-通过, 2-驳回）");
        }

        if (auditStatus == 2 && (auditRemark == null || auditRemark.trim().isEmpty())) {
            return Result.error(400, "驳回必须填写原因");
        }

        MusicVO updated = singerService.auditSong(musicId, auditStatus, auditRemark);
        return updated == null ? Result.error(404, "歌曲不存在") : Result.success("审核成功", updated);
    }

    /**
     * 查询当前歌手的歌曲列表（我的歌曲）
     */
    @GetMapping("/mySongs")
    public Result<PageResult<MusicVO>> mySongs(@RequestHeader("X-User-Id") String userIdHeader,
                                                @RequestHeader("X-Role") String roleHeader,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer auditStatus) {
        if (!"1".equals(roleHeader)) {
            return Result.error(403, "仅歌手可以查看自己的歌曲");
        }

        Integer userId;
        try {
            userId = Integer.parseInt(userIdHeader);
        } catch (NumberFormatException e) {
            return Result.error(401, "用户身份无效");
        }

        return Result.success(singerService.pageSongs(userId, page, size, keyword, null, auditStatus));
    }
}
