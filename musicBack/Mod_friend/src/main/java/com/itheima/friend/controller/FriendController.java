package com.itheima.friend.controller;

import com.itheima.domain.common.Result;
import com.itheima.friend.entity.Friend;
import com.itheima.friend.entity.FriendRequest;
import com.itheima.friend.service.FriendService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友接口
 * <p>
 * 网关路由：/api/friend/** → StripPrefix=1 → /friend/**
 * <p>
 * 所有接口均需登录，网关解析 JWT 后通过 X-User-Id 头透传用户ID。
 */
@RestController
@RequestMapping("/friend")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    // ======================== 搜索用户 ========================

    /**
     * 搜索用户（排除已好友）
     * <p>
     * GET /friend/search?keyword=xxx
     */
    @GetMapping("/search")
    public Result<List<com.itheima.domain.entity.User>> searchUsers(
            @RequestParam String keyword,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(friendService.searchUsers(userId, keyword));
    }

    // ======================== 好友请求 ========================

    /**
     * 发送好友请求
     * <p>
     * POST /friend/request  body: { receiverId, message }
     */
    @PostMapping("/request")
    public Result<FriendRequest> sendRequest(
            @RequestBody FriendRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        FriendRequest result = friendService.sendRequest(userId, request.getReceiverId(), request.getMessage());
        return Result.success("好友请求已发送", result);
    }

    /**
     * 获取收到的好友请求
     * <p>
     * GET /friend/request/received
     */
    @GetMapping("/request/received")
    public Result<List<FriendRequest>> getReceivedRequests(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(friendService.getReceivedRequests(userId));
    }

    /**
     * 获取发送的好友请求
     * <p>
     * GET /friend/request/sent
     */
    @GetMapping("/request/sent")
    public Result<List<FriendRequest>> getSentRequests(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(friendService.getSentRequests(userId));
    }

    /**
     * 接受好友请求
     * <p>
     * PUT /friend/request/accept/{id}
     */
    @PutMapping("/request/accept/{id}")
    public Result<Void> acceptRequest(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        friendService.acceptRequest(id, userId);
        return Result.success();
    }

    /**
     * 拒绝好友请求
     * <p>
     * PUT /friend/request/reject/{id}
     */
    @PutMapping("/request/reject/{id}")
    public Result<Void> rejectRequest(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        friendService.rejectRequest(id, userId);
        return Result.success();
    }

    // ======================== 好友管理 ========================

    /**
     * 获取好友列表
     * <p>
     * GET /friend/list
     */
    @GetMapping("/list")
    public Result<List<Friend>> getFriendList(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(friendService.getFriendList(userId));
    }

    /**
     * 删除好友
     * <p>
     * DELETE /friend/{friendId}
     */
    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(
            @PathVariable Integer friendId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        friendService.deleteFriend(userId, friendId);
        return Result.success();
    }
}