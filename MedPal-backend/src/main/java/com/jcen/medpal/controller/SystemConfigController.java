package com.jcen.medpal.controller;

import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.SystemConfig;
import com.jcen.medpal.service.SystemConfigService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/system-config")
public class SystemConfigController {

    @Resource
    private SystemConfigService systemConfigService;

    @GetMapping("/get")
    public Object getConfig(@RequestParam String key) {
        try {
            var result = systemConfigService.getConfig(key);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/update")
    public Object updateConfig(@RequestBody SystemConfig config) {
        try {
            boolean result = systemConfigService.updateConfig(config);
            return result ? ResultUtils.success("更新成功") : ResultUtils.error(40000, "更新失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "更新失败");
        }
    }

    @GetMapping("/list")
    public Object listConfigs() {
        try {
            var result = systemConfigService.listConfigs();
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/price")
    public Object getPriceConfig() {
        try {
            var result = systemConfigService.getPriceConfig();
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/price/update")
    public Object updatePriceConfig(@RequestBody Object priceConfig) {
        try {
            boolean result = systemConfigService.updatePriceConfig(priceConfig);
            return result ? ResultUtils.success("更新成功") : ResultUtils.error(40000, "更新失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "更新失败");
        }
    }
}
