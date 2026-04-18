package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.EmergencyHelp;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.EmergencyHelpService;
import com.jcen.medpal.service.UserService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 紧急求助控制器
 */
@RestController
@RequestMapping("/emergency")
public class EmergencyHelpController {

    @Resource
    private EmergencyHelpService emergencyHelpService;

    @Resource
    private UserService userService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/create")
    public Object createEmergencyHelp(@RequestBody EmergencyHelp emergencyHelp, HttpServletRequest request) {
        try {
            ensureEmergencyHelpTable();
            User loginUser = userService.getLoginUser(request);
            emergencyHelp.setUserId(loginUser.getId());
            EmergencyHelp result = emergencyHelpService.createEmergencyHelp(emergencyHelp);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "发起求助失败" : e.getMessage());
        }
    }

    @GetMapping("/list")
    public Object listEmergencyHelps(@RequestParam(defaultValue = "1") long current,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) Long userId,
                                      @RequestParam(required = false) String status,
                                      HttpServletRequest request) {
        try {
            ensureEmergencyHelpTable();
            User loginUser = userService.getLoginUser(request);
            boolean isAdmin = "admin".equals(loginUser.getUserRole());
            boolean isCompanion = "companion".equals(loginUser.getUserRole());
            Page<EmergencyHelp> page = new Page<>(current, size);
            Long queryUserId = userId;
            if (!isAdmin) {
                if (isCompanion) {
                    queryUserId = null;
                } else {
                    queryUserId = loginUser.getId();
                }
            }
            IPage<EmergencyHelp> result = emergencyHelpService.lambdaQuery()
                    .eq(queryUserId != null, EmergencyHelp::getUserId, queryUserId)
                    .eq(status != null && !status.trim().isEmpty(), EmergencyHelp::getStatus, status)
                    .orderByDesc(EmergencyHelp::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/{id}")
    public Object getEmergencyHelp(@PathVariable Long id, HttpServletRequest request) {
        try {
            ensureEmergencyHelpTable();
            User loginUser = userService.getLoginUser(request);
            EmergencyHelp emergencyHelp = emergencyHelpService.getById(id);
            if (emergencyHelp == null) {
                return ResultUtils.error(40400, "求助不存在");
            }
            boolean isAdmin = "admin".equals(loginUser.getUserRole());
            boolean isCompanion = "companion".equals(loginUser.getUserRole());
            boolean isOwner = emergencyHelp.getUserId() != null && emergencyHelp.getUserId().equals(loginUser.getId());
            boolean isResponder = emergencyHelp.getResponderId() != null && emergencyHelp.getResponderId().equals(loginUser.getId());
            if (!isAdmin && !isOwner && !isCompanion && !isResponder) {
                return ResultUtils.error(40300, "无权限查看该求助");
            }
            return ResultUtils.success(emergencyHelp);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/respond/{id}")
    public Object respondEmergencyHelp(@PathVariable Long id, HttpServletRequest request) {
        try {
            ensureEmergencyHelpTable();
            User loginUser = userService.getLoginUser(request);
            if (!"companion".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
                return ResultUtils.error(40300, "仅陪诊员可响应");
            }
            boolean result = emergencyHelpService.respondEmergencyHelp(id, loginUser.getId());
            return result ? ResultUtils.success("响应成功") : ResultUtils.error(40000, "响应失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "响应失败");
        }
    }

    @PostMapping("/resolve/{id}")
    public Object resolveEmergencyHelp(@PathVariable Long id,
                                       @RequestParam(required = false) String resolveNote,
                                       HttpServletRequest request) {
        try {
            ensureEmergencyHelpTable();
            User loginUser = userService.getLoginUser(request);
            boolean isAdmin = "admin".equals(loginUser.getUserRole());
            boolean result = emergencyHelpService.resolveEmergencyHelp(id, loginUser.getId(), isAdmin, resolveNote);
            return result ? ResultUtils.success("求助已解决") : ResultUtils.error(40000, "处理失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "处理失败" : e.getMessage());
        }
    }

    private void ensureEmergencyHelpTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS emergency_help (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'id'," +
                "user_id BIGINT NOT NULL COMMENT '用户ID'," +
                "content TEXT NULL COMMENT '求助内容'," +
                "location VARCHAR(512) NULL COMMENT '位置描述'," +
                "latitude DECIMAL(10,8) NULL COMMENT '纬度'," +
                "longitude DECIMAL(11,8) NULL COMMENT '经度'," +
                "status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/responded/resolved'," +
                "responder_id BIGINT NULL COMMENT '响应陪诊员ID'," +
                "resolve_note VARCHAR(512) NULL COMMENT '处理说明'," +
                "resolve_time DATETIME NULL COMMENT '处理完成时间'," +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除'," +
                "KEY idx_user_id (user_id)," +
                "KEY idx_status (status)," +
                "KEY idx_responder_id (responder_id)" +
                ") COMMENT='紧急求助表' COLLATE=utf8mb4_unicode_ci");
    }
}
