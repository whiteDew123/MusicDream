package com.itheima.capsule.controller;

import com.itheima.capsule.dto.CapsuleDTO;
import com.itheima.capsule.service.CapsuleService;
import com.itheima.capsule.vo.CapsuleVO;
import com.itheima.domain.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 时空胶囊接口
 */
@RestController
@RequestMapping("/capsule")
@RequiredArgsConstructor
public class CapsuleController {

    private final CapsuleService capsuleService;

    /**
     * 创建胶囊
     */
    @PostMapping
    public Result<CapsuleVO> create(@RequestHeader("X-User-Id") Integer userId,
                                   @RequestBody CapsuleDTO dto) {
        return Result.success(capsuleService.createCapsule(userId, dto));
    }

    /**
     * 我创建的胶囊
     */
    @GetMapping("/my")
    public Result<List<CapsuleVO>> myCapsules(@RequestHeader("X-User-Id") Integer userId) {
        return Result.success(capsuleService.getMyCapsules(userId));
    }

    /**
     * 写给我的胶囊
     */
    @GetMapping("/received")
    public Result<List<CapsuleVO>> receivedCapsules(@RequestHeader("X-User-Id") Integer userId) {
        return Result.success(capsuleService.getReceivedCapsules(userId));
    }

    /**
     * 胶囊详情（封印状态隐藏留言）
     */
    @GetMapping("/{id}")
    public Result<CapsuleVO> detail(@PathVariable Integer id,
                                     @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        CapsuleVO vo = capsuleService.getCapsuleDetail(id, userId);
        return vo == null ? Result.error(404, "胶囊不存在") : Result.success(vo);
    }

    /**
     * 时空广场（公开，无需登录）
     */
    @GetMapping("/plaza")
    public Result<List<CapsuleVO>> plaza(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(capsuleService.getPlazaList(userId, size));
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like/{id}")
    public Result<Boolean> toggleLike(@PathVariable Integer id,
                                      @RequestHeader("X-User-Id") Integer userId) {
        return Result.success(capsuleService.toggleLike(id, userId));
    }

    /**
     * 设为公开
     */
    @PutMapping("/public/{id}")
    public Result<Boolean> makePublic(@PathVariable Integer id,
                                      @RequestHeader("X-User-Id") Integer userId) {
        return Result.success(capsuleService.makePublic(id, userId));
    }

    /**
     * 删除胶囊
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Integer id,
                                  @RequestHeader("X-User-Id") Integer userId) {
        return Result.success(capsuleService.deleteCapsule(id, userId));
    }
}
