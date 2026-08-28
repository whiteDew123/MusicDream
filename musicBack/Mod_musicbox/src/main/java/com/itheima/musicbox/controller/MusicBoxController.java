package com.itheima.musicbox.controller;

import com.itheima.domain.common.Result;
import com.itheima.musicbox.dto.MusicBoxCreateDTO;
import com.itheima.musicbox.dto.MusicBoxFriendRequestDTO;
import com.itheima.musicbox.service.MusicBoxService;
import com.itheima.musicbox.vo.MusicBoxFriendRequestVO;
import com.itheima.musicbox.vo.MusicBoxPlazaVO;
import com.itheima.musicbox.vo.MusicBoxVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 音乐盲盒接口
 * <p>
 * 网关路由：/api/musicbox/** → StripPrefix=1 → /musicbox/**
 * <p>
 * 所有接口均需登录，网关解析 JWT 后通过 X-User-Id 头透传用户ID。
 */
@RestController
@RequestMapping("/musicbox")
public class MusicBoxController {

    private final MusicBoxService musicBoxService;

    public MusicBoxController(MusicBoxService musicBoxService) {
        this.musicBoxService = musicBoxService;
    }

    // ======================== 创建盲盒 ========================

    /**
     * 创建盲盒
     * <p>
     * POST /musicbox/create  body: { title, moodTag, message, songIds }
     */
    @PostMapping("/create")
    public Result<Integer> createBox(
            @RequestBody MusicBoxCreateDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Integer boxId = musicBoxService.createBox(userId, dto);
        return Result.success(boxId);
    }

    // ======================== 盲盒广场 ========================

    /**
     * 盲盒广场列表（最新）
     * <p>
     * GET /musicbox/plaza?page=1&size=20
     */
    @GetMapping("/plaza")
    public Result<List<MusicBoxPlazaVO>> getPlazaList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(musicBoxService.getPlazaList(userId, page, size));
    }

    /**
     * 按标签筛选盲盒广场
     * <p>
     * GET /musicbox/plaza/tag?tag=深夜&page=1&size=20
     */
    @GetMapping("/plaza/tag")
    public Result<List<MusicBoxPlazaVO>> getPlazaListByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(musicBoxService.getPlazaListByTag(userId, tag, page, size));
    }

    /**
     * 热门排行
     * <p>
     * GET /musicbox/plaza/hot?limit=20
     */
    @GetMapping("/plaza/hot")
    public Result<List<MusicBoxPlazaVO>> getHotBoxes(
            @RequestParam(defaultValue = "20") int limit,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(musicBoxService.getHotBoxes(userId, limit));
    }

    /**
     * 随机推荐
     * <p>
     * GET /musicbox/plaza/random?limit=10
     */
    @GetMapping("/plaza/random")
    public Result<List<MusicBoxPlazaVO>> getRandomBoxes(
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(musicBoxService.getRandomBoxes(userId, limit));
    }

    // ======================== 盲盒互动 ========================

    /**
     * 获取盲盒详情（不记录开启次数）
     * <p>
     * GET /musicbox/{id}
     */
    @GetMapping("/{id}")
    public Result<MusicBoxVO> getBoxDetail(
            @PathVariable("id") Integer boxId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(musicBoxService.getBoxDetail(boxId, userId));
    }

    /**
     * 开启盲盒
     * <p>
     * POST /musicbox/{id}/open
     */
    @PostMapping("/{id}/open")
    public Result<MusicBoxVO> openBox(
            @PathVariable("id") Integer boxId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(musicBoxService.openBox(boxId, userId));
    }

    /**
     * 点赞/取消点赞盲盒
     * <p>
     * POST /musicbox/{id}/like
     */
    @PostMapping("/{id}/like")
    public Result<Void> toggleLike(
            @PathVariable("id") Integer boxId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        musicBoxService.toggleLike(boxId, userId);
        return Result.success();
    }

    // ======================== 我的盲盒 ========================

    /**
     * 我创建的盲盒
     * <p>
     * GET /musicbox/my
     */
    @GetMapping("/my")
    public Result<List<MusicBoxPlazaVO>> getMyBoxes(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(musicBoxService.getMyBoxes(userId));
    }

    /**
     * 我开启过的盲盒
     * <p>
     * GET /musicbox/opened
     */
    @GetMapping("/opened")
    public Result<List<MusicBoxPlazaVO>> getOpenedBoxes(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(musicBoxService.getOpenedBoxes(userId));
    }

    /**
     * 我点赞过的盲盒
     * <p>
     * GET /musicbox/liked
     */
    @GetMapping("/liked")
    public Result<List<MusicBoxPlazaVO>> getLikedBoxes(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(musicBoxService.getLikedBoxes(userId));
    }

    /**
     * 删除盲盒
     * <p>
     * DELETE /musicbox/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteBox(
            @PathVariable("id") Integer boxId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        musicBoxService.deleteBox(boxId, userId);
        return Result.success();
    }

    // ======================== 盲盒交友 ========================

    /**
     * 发送盲盒交友请求
     * <p>
     * POST /musicbox/{id}/friend-request  body: { receiverId, message }
     */
    @PostMapping("/{id}/friend-request")
    public Result<Void> sendFriendRequest(
            @PathVariable("id") Integer boxId,
            @RequestBody MusicBoxFriendRequestDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        musicBoxService.sendFriendRequest(boxId, userId, dto.getReceiverId(), dto.getMessage());
        return Result.success();
    }

    /**
     * 查询收到的盲盒交友请求
     * <p>
     * GET /musicbox/friend-request/received
     */
    @GetMapping("/friend-request/received")
    public Result<List<MusicBoxFriendRequestVO>> getReceivedFriendRequests(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(musicBoxService.getReceivedFriendRequests(userId));
    }

    /**
     * 接受盲盒交友请求
     * <p>
     * PUT /musicbox/friend-request/accept/{id}
     */
    @PutMapping("/friend-request/accept/{id}")
    public Result<Void> acceptFriendRequest(
            @PathVariable("id") Integer requestId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        musicBoxService.acceptFriendRequest(requestId, userId);
        return Result.success();
    }

    /**
     * 拒绝盲盒交友请求
     * <p>
     * PUT /musicbox/friend-request/reject/{id}
     */
    @PutMapping("/friend-request/reject/{id}")
    public Result<Void> rejectFriendRequest(
            @PathVariable("id") Integer requestId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        musicBoxService.rejectFriendRequest(requestId, userId);
        return Result.success();
    }
}