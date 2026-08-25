package com.itheima.msg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.domain.common.Result;
import com.itheima.msg.entity.SongComment;
import com.itheima.msg.service.SongCommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 歌曲评论接口
 * <p>
 * 网关路由：/api/comment/** → StripPrefix=1 → /comment/**
 * <p>
 * - GET /comment/music/{musicId} 公开访问（已加入网关白名单）
 * - POST/DELETE 需登录，通过 X-User-Id / X-Role 透传
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final SongCommentService songCommentService;

    public CommentController(SongCommentService songCommentService) {
        this.songCommentService = songCommentService;
    }

    /**
     * 分页查询某首歌的一级评论列表
     * <p>
     * GET /comment/music/{musicId}?pn=1&size=20
     */
    @GetMapping("/music/{musicId}")
    public Result<Page<SongComment>> listComments(
            @PathVariable Integer musicId,
            @RequestParam(defaultValue = "1") Integer pn,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<SongComment> page = songCommentService.listComments(musicId, pn, size);
        return Result.success(page);
    }

    /**
     * 查询某条评论的楼中楼回复
     * <p>
     * GET /comment/{parentId}/replies
     */
    @GetMapping("/{parentId}/replies")
    public Result<List<SongComment>> listReplies(@PathVariable Long parentId) {
        List<SongComment> replies = songCommentService.listReplies(parentId);
        return Result.success(replies);
    }

    /**
     * 发表评论
     * <p>
     * POST /comment/music/{musicId}
     * body: {"content": "好听！", "parentId": null, "toUserId": null}
     */
    @PostMapping("/music/{musicId}")
    public Result<SongComment> publish(
            @PathVariable Integer musicId,
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-User-Id") Integer userId) {
        String content = (String) body.get("content");
        if (content == null || content.isBlank()) {
            return Result.fail("评论内容不能为空");
        }
        if (content.length() > 500) {
            return Result.fail("评论内容不能超过500字");
        }
        Long parentId = body.get("parentId") == null ? null : Long.valueOf(body.get("parentId").toString());
        Integer toUserId = body.get("toUserId") == null ? null : Integer.valueOf(body.get("toUserId").toString());
        SongComment comment = songCommentService.publish(musicId, userId, content.trim(), parentId, toUserId);
        return Result.success("评论成功", comment);
    }

    /**
     * 删除评论（仅作者或管理员）
     * <p>
     * DELETE /comment/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Integer userId,
            @RequestHeader(value = "X-Role", required = false) Integer role) {
        boolean ok = songCommentService.delete(id, userId, role);
        if (!ok) {
            return Result.fail("无权删除该评论或评论不存在");
        }
        return Result.success();
    }

    /**
     * 评论点赞（+1）
     * <p>
     * POST /comment/{id}/like
     */
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        boolean ok = songCommentService.like(id);
        if (!ok) {
            return Result.fail("评论不存在");
        }
        return Result.success();
    }

    /**
     * 查询某首歌的评论数
     * <p>
     * GET /comment/music/{musicId}/count
     */
    @GetMapping("/music/{musicId}/count")
    public Result<Integer> getCommentCount(@PathVariable Integer musicId) {
        int count = songCommentService.countByMusicId(musicId);
        return Result.success(count);
    }
}
