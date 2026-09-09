package com.itheima.admin.service;

import com.itheima.domain.common.Result;

/**
 * 标签字典管理服务
 */
public interface TagService {

    /** 分页查询标签（关键字模糊匹配类别码/名称） */
    Result pageTags(Integer pn, Integer size, String keyword);

    /** 标签使用频次统计（按次数降序） */
    Result statsTags();

    /** 新增标签（默认启用） */
    Result addTag(String code, String name);

    /** 编辑标签（允许修改类别码） */
    Result updateTag(Integer tagId, String code, String name);

    /** 启用/禁用标签 */
    Result updateTagStatus(Integer tagId, Integer status);

    /** 删除标签（被歌曲引用时禁止删除） */
    Result deleteTag(Integer tagId);
}