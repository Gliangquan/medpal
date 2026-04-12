package com.jcen.medpal.controller;

import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.UserSettings;
import com.jcen.medpal.service.UserSettingsService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/user-settings")
public class UserSettingsController {

    @Resource
    private UserSettingsService userSettingsService;

    @GetMapping("/get")
    public Object getSettings(@RequestParam Long userId) {
        try {
            UserSettings settings = userSettingsService.getUserSettings(userId);
            return ResultUtils.success(settings);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/notification/update")
    public Object updateNotificationSettings(@RequestParam Long userId, @RequestBody UserSettings settings) {
        try {
            boolean result = userSettingsService.updateNotificationSettings(userId, settings);
            return result ? ResultUtils.success("更新成功") : ResultUtils.error(40000, "更新失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "更新失败");
        }
    }

    @PostMapping("/privacy/update")
    public Object updatePrivacySettings(@RequestParam Long userId, @RequestBody UserSettings settings) {
        try {
            boolean result = userSettingsService.updatePrivacySettings(userId, settings);
            return result ? ResultUtils.success("更新成功") : ResultUtils.error(40000, "更新失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "更新失败");
        }
    }
}
