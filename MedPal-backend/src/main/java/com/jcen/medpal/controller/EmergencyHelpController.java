package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.EmergencyHelp;
import com.jcen.medpal.service.EmergencyHelpService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 紧急求助控制器
 */
@RestController
@RequestMapping("/emergency")
public class EmergencyHelpController {

    @Resource
    private EmergencyHelpService emergencyHelpService;

    @PostMapping("/create")
    public Object createEmergencyHelp(@RequestBody EmergencyHelp emergencyHelp) {
        try {
            EmergencyHelp result = emergencyHelpService.createEmergencyHelp(emergencyHelp);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "发起求助失败");
        }
    }

    @GetMapping("/list")
    public Object listEmergencyHelps(@RequestParam(defaultValue = "1") long current,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) Long userId,
                                      @RequestParam(required = false) String status) {
        try {
            Page<EmergencyHelp> page = new Page<>(current, size);
            IPage<EmergencyHelp> result = emergencyHelpService.lambdaQuery()
                    .eq(userId != null, EmergencyHelp::getUserId, userId)
                    .eq(status != null, EmergencyHelp::getStatus, status)
                    .orderByDesc(EmergencyHelp::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/{id}")
    public Object getEmergencyHelp(@PathVariable Long id) {
        try {
            EmergencyHelp emergencyHelp = emergencyHelpService.getById(id);
            return ResultUtils.success(emergencyHelp);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/respond/{id}")
    public Object respondEmergencyHelp(@PathVariable Long id, @RequestParam Long responderId) {
        try {
            boolean result = emergencyHelpService.respondEmergencyHelp(id, responderId);
            return result ? ResultUtils.success("响应成功") : ResultUtils.error(40000, "响应失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "响应失败");
        }
    }

    @PostMapping("/resolve/{id}")
    public Object resolveEmergencyHelp(@PathVariable Long id) {
        try {
            boolean result = emergencyHelpService.resolveEmergencyHelp(id);
            return result ? ResultUtils.success("求助已解决") : ResultUtils.error(40000, "处理失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "处理失败");
        }
    }
}
