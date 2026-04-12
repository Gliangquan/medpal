package com.jcen.medpal.controller;

import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.service.IncomeService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.time.LocalDate;

@RestController
@RequestMapping("/income")
public class IncomeController {

    @Resource
    private IncomeService incomeService;

    @GetMapping("/detail")
    public Object getIncomeDetail(@RequestParam Long companionId,
                                  @RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate) {
        try {
            var result = incomeService.getIncomeDetail(companionId, startDate, endDate);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/statistics")
    public Object getIncomeStatistics(@RequestParam Long companionId,
                                      @RequestParam String type) {
        try {
            var result = incomeService.getIncomeStatistics(companionId, type);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "统计失败");
        }
    }

    @GetMapping("/total")
    public Object getTotalIncome(@RequestParam Long companionId) {
        try {
            var result = incomeService.getTotalIncome(companionId);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/settlement-rule")
    public Object getSettlementRule() {
        try {
            var result = incomeService.getSettlementRule();
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }
}
