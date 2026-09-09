package com.itheima.admin.controller;

import com.itheima.admin.mapper.TagMapper;
import com.itheima.admin.service.LogService;
import com.itheima.admin.service.TagService;
import com.itheima.domain.common.Result;
import com.itheima.domain.entity.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 标签字典管理接口（管理员）
 */
@RestController
@RequestMapping("/admin/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final LogService logService;
    private final TagMapper tagMapper;

    /** 分页查询标签列表（含使用频次） */
    @GetMapping("/page/{pn}/{size}")
    public Result pageTags(@PathVariable(value = "pn") Integer pn,
                           @PathVariable(value = "size") Integer size,
                           @RequestParam(value = "keyword", required = false) String keyword) {
        return tagService.pageTags(pn, size, keyword);
    }

    /** 标签使用频次统计（按次数降序） */
    @GetMapping("/stats")
    public Result stats() {
        return tagService.statsTags();
    }

    /** 新增标签 */
    @PostMapping("/add")
    public Result addTag(@RequestParam(value = "code") String code,
                         @RequestParam(value = "name") String name,
                         HttpServletRequest request) {
        Result result = tagService.addTag(code, name);
        if (result.getCode() == 200) {
            logService.saveLog(getOperator(request), "新增标签", code + ":" + name);
        }
        return result;
    }

    /** 编辑标签 */
    @PostMapping("/update")
    public Result updateTag(@RequestParam(value = "tagId") Integer tagId,
                            @RequestParam(value = "code") String code,
                            @RequestParam(value = "name") String name,
                            HttpServletRequest request) {
        Result result = tagService.updateTag(tagId, code, name);
        if (result.getCode() == 200) {
            logService.saveLog(getOperator(request), "编辑标签", code + ":" + name);
        }
        return result;
    }

    /** 启用/禁用标签 */
    @PostMapping("/status")
    public Result updateStatus(@RequestParam(value = "tagId") Integer tagId,
                               @RequestParam(value = "status") Integer status,
                               HttpServletRequest request) {
        Result result = tagService.updateTagStatus(tagId, status);
        if (result.getCode() == 200) {
            Tag tag = tagMapper.selectById(tagId);
            String tagName = tag != null ? tag.getCode() + ":" + tag.getName() : "未知标签";
            logService.saveLog(getOperator(request), status == 1 ? "启用标签" : "禁用标签", tagName);
        }
        return result;
    }

    /** 删除标签 */
    @PostMapping("/delete")
    public Result deleteTag(@RequestParam(value = "tagId") Integer tagId,
                            HttpServletRequest request) {
        Tag tag = tagMapper.selectById(tagId);
        String tagName = tag != null ? tag.getCode() + ":" + tag.getName() : "未知标签";
        Result result = tagService.deleteTag(tagId);
        if (result.getCode() == 200) {
            logService.saveLog(getOperator(request), "删除标签", tagName);
        }
        return result;
    }

    private String getOperator(HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        return username != null && !username.isEmpty() ? username : "未知操作者";
    }
}