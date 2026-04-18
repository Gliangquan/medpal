package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.TrainingCourse;
import com.jcen.medpal.model.entity.TrainingRecord;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.TrainingService;
import com.jcen.medpal.service.UserService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/training")
public class TrainingController {

    @Resource
    private TrainingService trainingService;

    @Resource
    private UserService userService;

    @GetMapping("/courses")
    public Object listCourses(@RequestParam(defaultValue = "1") long current,
                              @RequestParam(defaultValue = "10") long size,
                              @RequestParam(required = false) String category) {
        try {
            Page<TrainingCourse> page = new Page<>(current, size);
            IPage<TrainingCourse> result = trainingService.lambdaQuery()
                    .eq(category != null, TrainingCourse::getCategory, category)
                    .eq(TrainingCourse::getStatus, 1)
                    .eq(TrainingCourse::getIsDelete, 0)
                    .orderByDesc(TrainingCourse::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    @GetMapping("/course/{id}")
    public Object getCourse(@PathVariable Long id) {
        try {
            TrainingCourse course = trainingService.getCourseById(id);
            return ResultUtils.success(course);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    @PostMapping("/record")
    public Object recordLearning(@RequestBody TrainingRecord record, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            if (!"companion".equals(loginUser.getUserRole())) {
                return ResultUtils.error(40300, "仅陪诊员可记录学习进度");
            }
            record.setCompanionId(loginUser.getId());
            TrainingRecord result = trainingService.recordLearning(record);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "记录失败" : e.getMessage());
        }
    }

    @GetMapping("/records")
    public Object getLearningRecords(@RequestParam(required = false) Long companionId,
                                    @RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size,
                                    HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            Long targetCompanionId = resolveCompanionId(companionId, loginUser);
            var result = trainingService.getLearningRecords(targetCompanionId, current, size);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    @GetMapping("/progress")
    public Object getLearningProgress(@RequestParam(required = false) Long companionId,
                                      @RequestParam Long courseId,
                                      HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            Long targetCompanionId = resolveCompanionId(companionId, loginUser);
            var result = trainingService.getLearningProgress(targetCompanionId, courseId);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    private Long resolveCompanionId(Long companionId, User loginUser) {
        boolean isAdmin = "admin".equals(loginUser.getUserRole());
        if (isAdmin) {
            if (companionId == null || companionId <= 0) {
                throw new IllegalArgumentException("陪诊员ID不能为空");
            }
            return companionId;
        }
        if (!"companion".equals(loginUser.getUserRole())) {
            throw new IllegalArgumentException("仅陪诊员可查看培训学习记录");
        }
        if (companionId != null && !companionId.equals(loginUser.getId())) {
            throw new IllegalArgumentException("无权限查看他人的学习记录");
        }
        return loginUser.getId();
    }
}
