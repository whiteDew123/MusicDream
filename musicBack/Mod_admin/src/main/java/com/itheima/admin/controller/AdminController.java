package com.itheima.admin.controller;

import com.itheima.admin.service.MusicService;
import com.itheima.admin.service.LogService;
import com.itheima.admin.service.UserService;
import com.itheima.domain.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private MusicService musicService;

    @Autowired
    private LogService logService;

    @GetMapping("/pageUser/{pn}/{size}")
    public Result pageUser(@PathVariable(value = "pn") Integer pn,
                           @PathVariable(value = "size") Integer size,
                           @RequestParam(value = "keyword", required = false) String keyword) {
        return userService.searchUser(pn, size, keyword);
    }

    @PostMapping("/freezeUser")
    public Result freezeUser(@RequestParam(value = "id") Integer id) {
        return userService.freezeUser(id);
    }

    @PostMapping("/unfreezeUser")
    public Result unfreezeUser(@RequestParam(value = "id") Integer id) {
        return userService.unFreezeUser(id);
    }

    @GetMapping("/pageMusic/{pn}/{size}")
    public Result pageMusic(@PathVariable(value = "pn") Integer pn,
                             @PathVariable(value = "size") Integer size,
                             @RequestParam(value = "keyword", required = false) String keyword) {
        return musicService.searchMusic(pn, size, keyword);
    }

    @PostMapping("/freezeMusic")
    public Result freezeMusic(@RequestParam(value = "id") Integer id) {
        return musicService.freezeMusic(id);
    }

    @PostMapping("/unfreezeMusic")
    public Result unfreezeMusic(@RequestParam(value = "id") Integer id) {
        return musicService.unFreezeMusic(id);
    }

    @GetMapping("/pagePendingMusic/{pn}/{size}")
    public Result pagePendingMusic(@PathVariable(value = "pn") Integer pn,
                                   @PathVariable(value = "size") Integer size) {
        return musicService.pagePendingMusic(pn, size);
    }

    @PostMapping("/approveMusic")
    public Result approveMusic(@RequestParam(value = "id") Integer id) {
        return musicService.approveMusic(id);
    }

    @PostMapping("/rejectMusic")
    public Result rejectMusic(@RequestParam(value = "id") Integer id,
                              @RequestParam(value = "remark", required = false) String remark) {
        return musicService.rejectMusic(id, remark);
    }

    @PostMapping("/deleteMusic")
    public Result deleteMusic(@RequestParam(value = "id") Integer id) {
        return musicService.deleteMusic(id);
    }

    @GetMapping("/pageLog/{pn}/{size}")
    public Result pageLog(@PathVariable(value = "pn") Integer pn,
                          @PathVariable(value = "size") Integer size,
                          @RequestParam(value = "keyword", required = false) String keyword) {
        return logService.searchLog(pn, size, keyword);
    }
}