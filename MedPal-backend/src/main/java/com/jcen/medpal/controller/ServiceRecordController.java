package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.ServiceRecord;
import com.jcen.medpal.service.ServiceRecordService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/service-record")
public class ServiceRecordController {

    @Resource
    private ServiceRecordService serviceRecordService;

    @PostMapping("/create")
    public Object createServiceRecord(@RequestBody ServiceRecord serviceRecord) {
        try {
            ServiceRecord result = serviceRecordService.createServiceRecord(serviceRecord);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "创建记录失败");
        }
    }

    @GetMapping("/list")
    public Object listServiceRecords(@RequestParam(defaultValue = "1") long current,
                                     @RequestParam(defaultValue = "10") long size,
                                     @RequestParam(required = false) Long companionId,
                                     @RequestParam(required = false) Long orderId) {
        try {
            Page<ServiceRecord> page = new Page<>(current, size);
            IPage<ServiceRecord> result = serviceRecordService.lambdaQuery()
                    .eq(companionId != null, ServiceRecord::getCompanionId, companionId)
                    .eq(orderId != null, ServiceRecord::getOrderId, orderId)
                    .orderByDesc(ServiceRecord::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/{id}")
    public Object getServiceRecord(@PathVariable Long id) {
        try {
            ServiceRecord serviceRecord = serviceRecordService.getById(id);
            return ResultUtils.success(serviceRecord);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/update/{id}")
    public Object updateServiceRecord(@PathVariable Long id, @RequestBody ServiceRecord serviceRecord) {
        try {
            serviceRecord.setId(id);
            boolean result = serviceRecordService.updateById(serviceRecord);
            return result ? ResultUtils.success("更新成功") : ResultUtils.error(40000, "更新失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "更新失败");
        }
    }
}
