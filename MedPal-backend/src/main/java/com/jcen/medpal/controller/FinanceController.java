package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.Settlement;
import com.jcen.medpal.service.FinanceService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/finance")
public class FinanceController {

    @Resource
    private FinanceService financeService;

    @GetMapping("/settlement/list")
    public Object listSettlements(@RequestParam(defaultValue = "1") long current,
                                  @RequestParam(defaultValue = "10") long size,
                                  @RequestParam(required = false) String status) {
        try {
            Page<Settlement> page = new Page<>(current, size);
            IPage<Settlement> result = financeService.lambdaQuery()
                    .eq(status != null, Settlement::getStatus, status)
                    .orderByDesc(Settlement::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/settlement/{id}")
    public Object processSettlement(@PathVariable Long id) {
        try {
            boolean result = financeService.processSettlement(id);
            return result ? ResultUtils.success("结算成功") : ResultUtils.error(40000, "结算失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "结算失败");
        }
    }

    @PostMapping("/settlement/batch")
    public Object batchProcessSettlements(@RequestParam String ids) {
        try {
            boolean result = financeService.batchProcessSettlements(ids);
            return result ? ResultUtils.success("批量结算成功") : ResultUtils.error(40000, "批量结算失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "批量结算失败");
        }
    }

    @GetMapping("/report")
    public Object getFinanceReport(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            var result = financeService.getFinanceReport(startDate, endDate);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/logs")
    public Object getFinanceLogs(@RequestParam(defaultValue = "1") long current,
                                 @RequestParam(defaultValue = "10") long size) {
        try {
            var result = financeService.getFinanceLogs(current, size);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }
}
