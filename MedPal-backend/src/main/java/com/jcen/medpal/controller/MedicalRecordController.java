package com.jcen.medpal.controller;

import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.MedicalRecord;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.MedicalRecordService;
import com.jcen.medpal.service.UserService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/medical-record")
public class MedicalRecordController {

    @Resource
    private MedicalRecordService medicalRecordService;

    @Resource
    private UserService userService;

    @PostMapping("/create")
    public Object createMedicalRecord(@RequestBody MedicalRecord record) {
        try {
            MedicalRecord result = medicalRecordService.createMedicalRecord(record);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "创建病历失败");
        }
    }

    @GetMapping("/list")
    public Object listMedicalRecords(@RequestParam(required = false) Long userId,
                                     @RequestParam(defaultValue = "1") long current,
                                     @RequestParam(defaultValue = "10") long size,
                                     HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            boolean isAdmin = "admin".equals(loginUser.getUserRole());
            Long targetUserId = userId;
            if (!isAdmin) {
                if (!"patient".equals(loginUser.getUserRole()) && !"user".equals(loginUser.getUserRole())) {
                    return ResultUtils.error(40300, "仅患者可查看病历");
                }
                targetUserId = loginUser.getId();
            }
            if (targetUserId == null) {
                return ResultUtils.error(40000, "用户ID不能为空");
            }
            var result = medicalRecordService.getUserMedicalRecords(targetUserId, current, size);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/{id}")
    public Object getMedicalRecord(@PathVariable Long id, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            MedicalRecord record = medicalRecordService.getById(id);
            if (record == null) {
                return ResultUtils.error(40400, "病历不存在");
            }
            boolean isAdmin = "admin".equals(loginUser.getUserRole());
            boolean isOwner = record.getUserId() != null && record.getUserId().equals(loginUser.getId());
            if (!isAdmin && !isOwner) {
                return ResultUtils.error(40300, "无权限查看该病历");
            }
            return ResultUtils.success(record);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/update/{id}")
    public Object updateMedicalRecord(@PathVariable Long id, @RequestBody MedicalRecord record) {
        try {
            record.setId(id);
            boolean result = medicalRecordService.updateById(record);
            return result ? ResultUtils.success("更新成功") : ResultUtils.error(40000, "更新失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "更新失败");
        }
    }

    @PostMapping("/delete/{id}")
    public Object deleteMedicalRecord(@PathVariable Long id) {
        try {
            boolean result = medicalRecordService.removeById(id);
            return result ? ResultUtils.success("删除成功") : ResultUtils.error(40000, "删除失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "删除失败");
        }
    }
}
