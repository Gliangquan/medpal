package com.jcen.medpal.controller;

import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.Notification;
import com.jcen.medpal.service.NotificationService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @GetMapping("/list")
    public Object listNotifications(@RequestParam Long userId,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(defaultValue = "1") long current,
                                   @RequestParam(defaultValue = "10") long size) {
        try {
            var result = notificationService.getUserNotifications(userId, status, current, size);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/unread-count")
    public Object getUnreadCount(@RequestParam Long userId) {
        try {
            long count = notificationService.getUnreadCount(userId);
            return ResultUtils.success(count);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/read/{id}")
    public Object markAsRead(@PathVariable Long id) {
        try {
            boolean result = notificationService.markAsRead(id);
            return result ? ResultUtils.success("已标记为已读") : ResultUtils.error(40000, "操作失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "操作失败");
        }
    }

    @PostMapping("/read-all")
    public Object markAllAsRead(@RequestParam Long userId) {
        try {
            boolean result = notificationService.markAllAsRead(userId);
            return result ? ResultUtils.success("全部已读") : ResultUtils.error(40000, "操作失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "操作失败");
        }
    }

    @PostMapping("/create")
    public Object createNotification(@RequestBody Notification notification) {
        try {
            Notification result = notificationService.createNotification(
                notification.getUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getRelatedType(),
                notification.getRelatedId()
            );
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "创建失败");
        }
    }
}
