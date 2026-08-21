package com.itheima.admin.controller;

import com.itheima.admin.mapper.MusicMapper;
import com.itheima.admin.mapper.UserMapper;
import com.itheima.admin.service.MusicService;
import com.itheima.admin.service.LogService;
import com.itheima.admin.service.UserService;
import com.itheima.admin.service.MonitorService;
import com.itheima.domain.common.Result;
import com.itheima.domain.entity.Music;
import com.itheima.domain.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final MusicService musicService;
    private final LogService logService;
    private final MonitorService monitorService;
    private final UserMapper userMapper;
    private final MusicMapper musicMapper;

    @GetMapping("/pageUser/{pn}/{size}")
    public Result pageUser(@PathVariable(value = "pn") Integer pn,
                           @PathVariable(value = "size") Integer size,
                           @RequestParam(value = "keyword", required = false) String keyword) {
        return userService.searchUser(pn, size, keyword);
    }

    @PostMapping("/freezeUser")
    public Result freezeUser(@RequestParam(value = "id") Integer id,
                             HttpServletRequest request) {
        Result result = userService.freezeUser(id);
        User user = userMapper.selectById(id);
        String userName = user != null ? user.getUsername() : "未知用户";
        logService.saveLog(getOperator(request), "冻结用户", userName);
        return result;
    }

    @PostMapping("/unfreezeUser")
    public Result unfreezeUser(@RequestParam(value = "id") Integer id,
                               HttpServletRequest request) {
        Result result = userService.unFreezeUser(id);
        User user = userMapper.selectById(id);
        String userName = user != null ? user.getUsername() : "未知用户";
        logService.saveLog(getOperator(request), "解冻用户", userName);
        return result;
    }

    @GetMapping("/pageMusic/{pn}/{size}")
    public Result pageMusic(@PathVariable(value = "pn") Integer pn,
                             @PathVariable(value = "size") Integer size,
                             @RequestParam(value = "keyword", required = false) String keyword) {
        return musicService.searchMusic(pn, size, keyword);
    }

    @PostMapping("/freezeMusic")
    public Result freezeMusic(@RequestParam(value = "id") Integer id,
                              HttpServletRequest request) {
        Music music = musicMapper.selectById(id);
        String musicName = music != null ? music.getMusicName() : "未知歌曲";
        Result result = musicService.freezeMusic(id);
        logService.saveLog(getOperator(request), "冻结歌曲", musicName);
        return result;
    }

    @PostMapping("/unfreezeMusic")
    public Result unfreezeMusic(@RequestParam(value = "id") Integer id,
                                HttpServletRequest request) {
        Music music = musicMapper.selectById(id);
        String musicName = music != null ? music.getMusicName() : "未知歌曲";
        Result result = musicService.unFreezeMusic(id);
        logService.saveLog(getOperator(request), "解冻歌曲", musicName);
        return result;
    }

    @GetMapping("/pagePendingMusic/{pn}/{size}")
    public Result pagePendingMusic(@PathVariable(value = "pn") Integer pn,
                                   @PathVariable(value = "size") Integer size) {
        return musicService.pagePendingMusic(pn, size);
    }

    @PostMapping("/approveMusic")
    public Result approveMusic(@RequestParam(value = "id") Integer id,
                               HttpServletRequest request) {
        Music music = musicMapper.selectById(id);
        String musicName = music != null ? music.getMusicName() : "未知歌曲";
        Result result = musicService.approveMusic(id);
        logService.saveLog(getOperator(request), "审核通过歌曲", musicName);
        return result;
    }

    @PostMapping("/rejectMusic")
    public Result rejectMusic(@RequestParam(value = "id") Integer id,
                              @RequestParam(value = "remark", required = false) String remark,
                              HttpServletRequest request) {
        Music music = musicMapper.selectById(id);
        String musicName = music != null ? music.getMusicName() : "未知歌曲";
        Result result = musicService.rejectMusic(id, remark);
        logService.saveLog(getOperator(request), "审核驳回歌曲", musicName);
        return result;
    }

    @PostMapping("/deleteMusic")
    public Result deleteMusic(@RequestParam(value = "id") Integer id,
                              HttpServletRequest request) {
        Music music = musicMapper.selectById(id);
        String musicName = music != null ? music.getMusicName() : "未知歌曲";
        Result result = musicService.deleteMusic(id);
        logService.saveLog(getOperator(request), "删除歌曲", musicName);
        return result;
    }

    @GetMapping("/pageLog/{pn}/{size}")
    public Result pageLog(@PathVariable(value = "pn") Integer pn,
                          @PathVariable(value = "size") Integer size,
                          @RequestParam(value = "keyword", required = false) String keyword) {
        return logService.searchLog(pn, size, keyword);
    }

    @GetMapping("/monitor")
    public Result monitor() {
        return Result.success(monitorService.getMonitor());
    }

    @GetMapping("/topMusic")
    public Result topMusic(@RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        return Result.success(monitorService.getTopMusic(limit));
    }

    @GetMapping("/trend")
    public Result trend(@RequestParam(value = "days", defaultValue = "7") Integer days) {
        return Result.success(monitorService.getTrend(days));
    }

    private String getOperator(HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        return username != null && !username.isEmpty() ? username : "未知操作者";
    }
}