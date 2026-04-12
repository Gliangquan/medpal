package com.jcen.medpal.model.vo;

import lombok.Data;

/**
 * 用户统计VO
 */
@Data
public class UserStatisticsVO {
    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 活跃用户数（最近7天）
     */
    private Long activeUsers;

    /**
     * 新增用户数（最近7天）
     */
    private Long newUsers;

    /**
     * 管理员数
     */
    private Long adminCount;

    /**
     * 普通用户数
     */
    private Long userCount;

    /**
     * 封禁用户数
     */
    private Long banCount;

    /**
     * 启用用户数
     */
    private Long enabledCount;

    /**
     * 禁用用户数
     */
    private Long disabledCount;

    /**
     * 今日登录数
     */
    private Long todayLoginCount;

    /**
     * 本周登录数
     */
    private Long weekLoginCount;

    /**
     * 本月登录数
     */
    private Long monthLoginCount;
}
