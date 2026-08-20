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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 歌手模块接口
 */
@RestController
@RequestMapping("/api/singer")
@RequiredArgsConstructor
public class SingerController {

    private final SingerService singerService;

    /**
     * 分页查询歌曲
     */
    @GetMapping("/songs")
    public Result<PageResult<MusicVO>> pageSongs(@RequestParam(required = false) Integer singerId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(singerService.pageSongs(singerId, page, size));
    }

    /**
     * 发布歌曲
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
     * 修改歌曲信息
     */
    @PutMapping("/songs/{musicId}")
    public Result<MusicVO> updateSong(@PathVariable Integer musicId, @RequestBody MusicDTO dto) {
        MusicVO updated = singerService.updateSong(musicId, dto);
        return updated == null ? Result.error(404, "歌曲不存在") : Result.success("修改成功", updated);
    }

    /**
     * 删除歌曲
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
     * 获取歌手数据（别名：/api/singer/{singerId}）
     */
    @GetMapping("/{singerId}")
    public Result<SingerVO> singerInfoAlias(@PathVariable Integer singerId) {
        return singerInfo(singerId);
    }
}
