package com.jcen.medpal.controller;

import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.service.impl.FinanceServiceImpl;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/finance")
public class FinanceController {

    @Resource
    private FinanceServiceImpl financeService;

    @GetMapping("/summary")
    public Object getFinanceSummary() {
        try {
            return ResultUtils.success(financeService.getFinanceSummary());
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    @GetMapping("/settlement/list")
    public Object listSettlements(@RequestParam(defaultValue = "1") long current,
                                  @RequestParam(defaultValue = "10") long size,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String keyword) {
        try {
            return ResultUtils.success(financeService.listSettlementOverview(current, size, status, keyword));
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    @PostMapping("/settlement/{id}")
    public Object processSettlement(@PathVariable Long id) {
        try {
            boolean result = financeService.processSettlement(id);
            return result ? ResultUtils.success("结算成功") : ResultUtils.error(40000, "结算失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "结算失败" : e.getMessage());
        }
    }

    @PostMapping("/settlement/batch")
    public Object batchProcessSettlements(@RequestParam String ids) {
        try {
            boolean result = financeService.batchProcessSettlements(ids);
            return result ? ResultUtils.success("批量结算成功") : ResultUtils.error(40000, "批量结算失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "批量结算失败" : e.getMessage());
        }
    }

    @GetMapping("/report")
    public Object getFinanceReport(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            var result = financeService.getFinanceReport(startDate, endDate);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    @GetMapping("/logs")
    public Object getFinanceLogs(@RequestParam(defaultValue = "1") long current,
                                 @RequestParam(defaultValue = "10") long size) {
        try {
            var result = financeService.getFinanceLogs(current, size);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }
}
