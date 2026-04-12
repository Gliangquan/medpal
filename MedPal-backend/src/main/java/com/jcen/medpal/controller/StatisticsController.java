package com.jcen.medpal.controller;

import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.service.StatisticsService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public Object getDashboardData() {
        try {
            var result = statisticsService.getDashboardData();
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/user")
    public Object getUserStatistics(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            var result = statisticsService.getUserStatistics(startDate, endDate);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/order")
    public Object getOrderStatistics(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            var result = statisticsService.getOrderStatistics(startDate, endDate);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/companion")
    public Object getCompanionStatistics(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            var result = statisticsService.getCompanionStatistics(startDate, endDate);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/revenue")
    public Object getRevenueStatistics(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            var result = statisticsService.getRevenueStatistics(startDate, endDate);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }
}
