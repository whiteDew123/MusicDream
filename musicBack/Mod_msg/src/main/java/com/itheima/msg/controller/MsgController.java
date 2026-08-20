package com.itheima.msg.controller;

import com.itheima.domain.common.Result;
import com.itheima.msg.dto.MsgVO;
import com.itheima.msg.dto.PublishMsgRequest;
import com.itheima.msg.entity.Msg;
import com.itheima.msg.service.MsgService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/msg")
public class MsgController {

    private final MsgService msgService;

    public MsgController(MsgService msgService) {
        this.msgService = msgService;
    }

    @PostMapping("/publish")
    public Result<Msg> publish(@RequestBody PublishMsgRequest request) {
        Msg msg = msgService.publish(request);
        return Result.success("消息发布成功", msg);
    }

    @GetMapping("/my")
    public Result<List<MsgVO>> getMyMessages(
            @RequestHeader("X-User-Id") Integer userId) {
        List<MsgVO> messages = msgService.getMyMessages(userId);
        return Result.success(messages);
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(
            @RequestHeader("X-User-Id") Integer userId) {
        int count = msgService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(
            @PathVariable Integer id,
            @RequestHeader("X-User-Id") Integer userId) {
        msgService.markAsRead(id, userId);
        return Result.success("已标记为已读", null);
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(
            @RequestHeader("X-User-Id") Integer userId) {
        msgService.markAllAsRead(userId);
        return Result.success("全部标记为已读", null);
    }
}