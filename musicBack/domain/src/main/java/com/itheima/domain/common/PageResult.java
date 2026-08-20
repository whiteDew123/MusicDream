package com.itheima.domain.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    /** 当前页码 */
    private Long current;

    /** 每页条数 */
    private Long size;

    /** 总条数 */
    private Long total;

    /** 总页数 */
    private Long pages;

    /** 当前页数据 */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Long current, Long size, Long total, Long pages, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = pages;
        this.records = records;
    }
}
