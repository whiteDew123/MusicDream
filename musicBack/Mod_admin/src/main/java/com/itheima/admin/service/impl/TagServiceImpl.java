package com.itheima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.admin.mapper.MusicTagMapper;
import com.itheima.admin.mapper.TagMapper;
import com.itheima.admin.service.TagService;
import com.itheima.domain.common.Result;
import com.itheima.domain.entity.MusicTag;
import com.itheima.domain.entity.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 标签字典管理服务实现
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final MusicTagMapper musicTagMapper;

    @Override
    public Result pageTags(Integer pn, Integer size, String keyword) {
        Page<Tag> page = new Page<>(pn, size);
        QueryWrapper<Tag> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(e -> e.like("code", keyword).or().like("name", keyword));
        }
        wrapper.orderByAsc("tag_id");
        Page<Tag> result = tagMapper.selectPage(page, wrapper);
        fillUsageCount(result.getRecords());
        return Result.success("查询成功", result);
    }

    @Override
    public Result statsTags() {
        List<Tag> list = tagMapper.selectWithUsageCount();
        return Result.success("统计成功", list);
    }

    @Override
    public Result addTag(String code, String name) {
        if (!StringUtils.hasText(code) || !StringUtils.hasText(name)) {
            return Result.fail(400, "类别码和标签名称不能为空");
        }
        String trimmedCode = code.trim();
        String trimmedName = name.trim();
        if (exists(trimmedCode, trimmedName, null)) {
            return Result.fail(400, "该类别下已存在同名标签");
        }
        Tag tag = new Tag();
        tag.setCode(trimmedCode);
        tag.setName(trimmedName);
        tag.setStatus(1);
        try {
            tagMapper.insert(tag);
        } catch (DuplicateKeyException e) {
            return Result.fail(400, "该类别下已存在同名标签");
        }
        return Result.success("新增成功", null);
    }

    @Override
    public Result updateTag(Integer tagId, String code, String name) {
        if (tagId == null || !StringUtils.hasText(code) || !StringUtils.hasText(name)) {
            return Result.fail(400, "参数不完整");
        }
        Tag exist = tagMapper.selectById(tagId);
        if (exist == null) {
            return Result.fail(404, "标签不存在");
        }
        String trimmedCode = code.trim();
        String trimmedName = name.trim();
        if (exists(trimmedCode, trimmedName, tagId)) {
            return Result.fail(400, "该类别下已存在同名标签");
        }
        exist.setCode(trimmedCode);
        exist.setName(trimmedName);
        try {
            tagMapper.updateById(exist);
        } catch (DuplicateKeyException e) {
            return Result.fail(400, "该类别下已存在同名标签");
        }
        return Result.success("修改成功", null);
    }

    @Override
    public Result updateTagStatus(Integer tagId, Integer status) {
        if (tagId == null || status == null || (status != 0 && status != 1)) {
            return Result.fail(400, "参数不合法");
        }
        Tag exist = tagMapper.selectById(tagId);
        if (exist == null) {
            return Result.fail(404, "标签不存在");
        }
        exist.setStatus(status);
        tagMapper.updateById(exist);
        return Result.success(status == 1 ? "启用成功" : "禁用成功", null);
    }

    @Override
    public Result deleteTag(Integer tagId) {
        Tag exist = tagMapper.selectById(tagId);
        if (exist == null) {
            return Result.fail(404, "标签不存在");
        }
        Long refCount = musicTagMapper.selectCount(new LambdaQueryWrapper<MusicTag>()
                .eq(MusicTag::getTagId, tagId));
        if (refCount != null && refCount > 0) {
            return Result.fail(400, "该标签已被 " + refCount + " 首歌曲使用，无法删除，可改为禁用");
        }
        tagMapper.deleteById(tagId);
        return Result.success("删除成功", null);
    }

    /** 校验 (code, name) 唯一性，excludeTagId 用于编辑时排除自身 */
    private boolean exists(String code, String name, Integer excludeTagId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<Tag>()
                .eq(Tag::getCode, code)
                .eq(Tag::getName, name);
        if (excludeTagId != null) {
            wrapper.ne(Tag::getTagId, excludeTagId);
        }
        return tagMapper.selectCount(wrapper) > 0;
    }

    /** 为分页结果填充使用频次 */
    private void fillUsageCount(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        for (Tag tag : tags) {
            Long count = musicTagMapper.selectCount(new LambdaQueryWrapper<MusicTag>()
                    .eq(MusicTag::getTagId, tag.getTagId()));
            tag.setUsageCount(count == null ? 0L : count);
        }
    }
}