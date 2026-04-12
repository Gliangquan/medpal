package com.jcen.medpal.controller;

import com.jcen.medpal.annotation.AuthCheck;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.constant.UserConstant;
import com.jcen.medpal.model.entity.Content;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.ContentService;
import com.jcen.medpal.service.NotificationService;
import com.jcen.medpal.service.UserService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/content")
public class ContentController {

    private static final Pattern MARKDOWN_EXTRA_PATTERN = Pattern.compile("\\[([^\\]]+)]\\([^\\)]+\\)");
    private static final int SUMMARY_MAX_LEN = 80;

    @Resource
    private ContentService contentService;

    @Resource
    private UserService userService;

    @Resource
    private NotificationService notificationService;

    @PostMapping("/create")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object createContent(@RequestBody Content content) {
        try {
            boolean shouldPublish = "published".equalsIgnoreCase(content.getStatus());
            Content result = contentService.createContent(content);
            if (shouldPublish) {
                boolean publishResult = contentService.publishContent(result.getId());
                if (publishResult) {
                    Content published = contentService.getById(result.getId());
                    sendPublishNotification(published);
                    result = published;
                }
            }
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "创建失败");
        }
    }

    @GetMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object listContents(@RequestParam(defaultValue = "1") long current,
                               @RequestParam(defaultValue = "10") long size,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String keyword) {
        try {
            Page<Content> page = new Page<>(current, size);
            IPage<Content> result = contentService.lambdaQuery()
                    .eq(type != null && !type.isEmpty(), Content::getType, type)
                    .eq(status != null && !status.isEmpty(), Content::getStatus, status)
                    .like(keyword != null && !keyword.isEmpty(), Content::getTitle, keyword)
                    .orderByDesc(Content::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object getContent(@PathVariable Long id) {
        try {
            Content content = contentService.getById(id);
            return ResultUtils.success(content);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/update/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object updateContent(@PathVariable Long id, @RequestBody Content content) {
        try {
            Content existed = contentService.getById(id);
            if (existed == null) {
                return ResultUtils.error(40000, "内容不存在");
            }
            String desiredStatus = content.getStatus();
            content.setId(id);
            boolean result = contentService.updateById(content);
            if (result && "published".equalsIgnoreCase(desiredStatus) && !isPublishedStatus(existed.getStatus())) {
                result = contentService.publishContent(id);
                if (result) {
                    Content published = contentService.getById(id);
                    sendPublishNotification(published);
                }
            }
            return result ? ResultUtils.success("更新成功") : ResultUtils.error(40000, "更新失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "更新失败");
        }
    }

    @PostMapping("/publish/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object publishContent(@PathVariable Long id) {
        try {
            Content existed = contentService.getById(id);
            if (existed == null) {
                return ResultUtils.error(40000, "内容不存在");
            }
            boolean result = contentService.publishContent(id);
            if (result && !isPublishedStatus(existed.getStatus())) {
                Content published = contentService.getById(id);
                sendPublishNotification(published);
            }
            return result ? ResultUtils.success("发布成功") : ResultUtils.error(40000, "发布失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "发布失败");
        }
    }

    @PostMapping("/unpublish/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object unpublishContent(@PathVariable Long id) {
        try {
            boolean result = contentService.unpublishContent(id);
            return result ? ResultUtils.success("下架成功") : ResultUtils.error(40000, "下架失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "下架失败");
        }
    }

    @PostMapping("/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object deleteContent(@PathVariable Long id) {
        try {
            boolean result = contentService.removeById(id);
            return result ? ResultUtils.success("删除成功") : ResultUtils.error(40000, "删除失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "删除失败");
        }
    }

    @GetMapping("/published/list")
    public Object listPublishedContents(@RequestParam(defaultValue = "1") long current,
                                        @RequestParam(defaultValue = "10") long size,
                                        @RequestParam(required = false) String type,
                                        @RequestParam(required = false) String keyword) {
        try {
            Page<Content> page = new Page<>(current, size);
            IPage<Content> result = contentService.lambdaQuery()
                    .eq(Content::getStatus, "published")
                    .eq(Content::getIsDelete, 0)
                    .eq(type != null && !type.isEmpty(), Content::getType, type)
                    .like(keyword != null && !keyword.isEmpty(), Content::getTitle, keyword)
                    .orderByDesc(Content::getPublishTime)
                    .orderByDesc(Content::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/published/{id}")
    public Object getPublishedContent(@PathVariable Long id) {
        try {
            Content content = contentService.lambdaQuery()
                    .eq(Content::getId, id)
                    .eq(Content::getStatus, "published")
                    .eq(Content::getIsDelete, 0)
                    .one();
            if (content == null) {
                return ResultUtils.error(40000, "内容不存在");
            }
            contentService.lambdaUpdate()
                    .eq(Content::getId, id)
                    .setSql("view_count = IFNULL(view_count, 0) + 1")
                    .update();
            content.setViewCount((content.getViewCount() == null ? 0 : content.getViewCount()) + 1);
            return ResultUtils.success(content);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    private void sendPublishNotification(Content content) {
        if (content == null || content.getId() == null || !"published".equalsIgnoreCase(content.getStatus())) {
            return;
        }
        Set<String> targets = parseTargets(content.getTags());
        List<String> userRoles;
        if (targets.contains("users") && targets.contains("companions")) {
            userRoles = Arrays.asList("patient", "user", "companion");
        } else if (targets.contains("companions")) {
            userRoles = Arrays.asList("companion");
        } else {
            userRoles = Arrays.asList("patient", "user");
        }
        List<User> users = userService.lambdaQuery()
                .eq(User::getStatus, 1)
                .in(User::getUserRole, userRoles)
                .list();
        if (users == null || users.isEmpty()) {
            return;
        }
        final String contentType = content.getType() == null ? "" : content.getType().toLowerCase(Locale.ROOT);
        final String noticeType = "announcement".equals(contentType) ? "system" : "activity";
        final String title = content.getTitle() == null || content.getTitle().isEmpty()
                ? ("announcement".equals(contentType) ? "平台公告" : "健康科普")
                : content.getTitle();
        final String noticeContent = buildNoticeContent(content);
        for (User user : users) {
            notificationService.createNotification(
                    user.getId(),
                    noticeType,
                    title,
                    noticeContent,
                    "content",
                    content.getId());
        }
    }

    private String buildNoticeContent(Content content) {
        if (content == null) {
            return "平台有新的内容发布";
        }
        String summary = content.getSummary();
        if (summary != null && !summary.trim().isEmpty()) {
            return truncate(summary.trim(), SUMMARY_MAX_LEN);
        }
        String raw = content.getContent();
        if (raw == null || raw.trim().isEmpty()) {
            return "平台有新的内容发布";
        }
        String plain = raw;
        plain = plain.replaceAll("`{1,3}", "");
        plain = plain.replaceAll("^#+\\s*", "");
        plain = plain.replaceAll("(\\*\\*|__|\\*|_)", "");
        plain = MARKDOWN_EXTRA_PATTERN.matcher(plain).replaceAll("$1");
        plain = plain.replaceAll("\\n+", " ").trim();
        if (plain.isEmpty()) {
            return "平台有新的内容发布";
        }
        return truncate(plain, SUMMARY_MAX_LEN);
    }

    private String truncate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "...";
    }

    private Set<String> parseTargets(String tags) {
        Set<String> result = new HashSet<>();
        if (tags == null || tags.trim().isEmpty()) {
            result.add("users");
            result.add("companions");
            return result;
        }
        String normalized = tags.toLowerCase(Locale.ROOT);
        String targetPart = normalized;
        int idx = normalized.indexOf("targets:");
        if (idx < 0) {
            idx = normalized.indexOf("targets=");
        }
        if (idx >= 0) {
            targetPart = normalized.substring(idx + 8);
        }
        String[] items = targetPart.split("[,;|\\s]+");
        for (String item : items) {
            String value = item.trim();
            if (value.isEmpty()) {
                continue;
            }
            if ("users".equals(value) || "user".equals(value) || "patient".equals(value)) {
                result.add("users");
            }
            if ("companions".equals(value) || "companion".equals(value)) {
                result.add("companions");
            }
        }
        if (result.isEmpty()) {
            result.add("users");
            result.add("companions");
        }
        return result;
    }

    private boolean isPublishedStatus(String status) {
        return status != null && "published".equalsIgnoreCase(status.trim());
    }
}
