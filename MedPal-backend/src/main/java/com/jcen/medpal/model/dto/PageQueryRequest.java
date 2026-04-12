package com.jcen.medpal.model.dto;

import lombok.Data;

/**
 * 分页查询请求基类
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
public class PageQueryRequest {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 页面大小
     */
    private Long pageSize = 10L;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（asc/desc）
     */
    private String sortOrder = "desc";
}
