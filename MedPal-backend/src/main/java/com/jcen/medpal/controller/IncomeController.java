package com.jcen.medpal.controller;

import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.IncomeService;
import com.jcen.medpal.service.UserService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/income")
public class IncomeController {

    @Resource
    private IncomeService incomeService;

    @Resource
    private UserService userService;

    @GetMapping("/detail")
    public Object getIncomeDetail(@RequestParam(required = false) Long companionId,
                                  @RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate,
                                  javax.servlet.http.HttpServletRequest request) {
        try {
            Long targetCompanionId = resolveCompanionId(companionId, request);
            var result = incomeService.getIncomeDetail(targetCompanionId, startDate, endDate);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Object getIncomeStatistics(@RequestParam(required = false) Long companionId,
                                      @RequestParam(defaultValue = "month") String type,
                                      javax.servlet.http.HttpServletRequest request) {
        try {
            Long targetCompanionId = resolveCompanionId(companionId, request);
            var result = incomeService.getIncomeStatistics(targetCompanionId, type);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "统计失败" : e.getMessage());
        }
    }

    @GetMapping("/total")
    public Object getTotalIncome(@RequestParam(required = false) Long companionId,
                                 javax.servlet.http.HttpServletRequest request) {
        try {
            Long targetCompanionId = resolveCompanionId(companionId, request);
            var result = incomeService.getTotalIncome(targetCompanionId);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    @GetMapping("/settlement-rule")
    public Object getSettlementRule() {
        try {
            var result = incomeService.getSettlementRule();
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    private Long resolveCompanionId(Long companionId, javax.servlet.http.HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = "admin".equals(loginUser.getUserRole());
        if (isAdmin) {
            if (companionId == null || companionId <= 0) {
                throw new IllegalArgumentException("陪诊员ID不能为空");
            }
            return companionId;
        }
        if (!"companion".equals(loginUser.getUserRole())) {
            throw new IllegalArgumentException("仅陪诊员可查看收入统计");
        }
        if (companionId != null && !companionId.equals(loginUser.getId())) {
            throw new IllegalArgumentException("无权限查看他人收入明细");
        }
        return loginUser.getId();
    }
}
