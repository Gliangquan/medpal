package com.jcen.medpal.controller;

import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.service.RecommendationService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 服务推荐控制器
 */
@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    @Resource
    private RecommendationService recommendationService;

    @GetMapping("/services")
    public Object recommendServices(@RequestParam Long userId) {
        try {
            var result = recommendationService.recommendServices(userId);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "推荐失败");
        }
    }

    @GetMapping("/hospitals")
    public Object recommendHospitals(@RequestParam Long userId) {
        try {
            var result = recommendationService.recommendHospitals(userId);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "推荐失败");
        }
    }

    @GetMapping("/top-companions")
    public Object getTopCompanions(@RequestParam(defaultValue = "10") int limit) {
        try {
            var result = recommendationService.getTopCompanions(limit);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/companions/by-specialty")
    public Object getCompanionsBySpecialty(@RequestParam String specialty) {
        try {
            var result = recommendationService.getCompanionsBySpecialty(specialty);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/gold-companions")
    public Object getGoldCompanions(@RequestParam(defaultValue = "10") int limit) {
        try {
            var result = recommendationService.getGoldCompanions(limit);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }
}
