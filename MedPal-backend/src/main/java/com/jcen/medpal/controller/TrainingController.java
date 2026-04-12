package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.TrainingCourse;
import com.jcen.medpal.model.entity.TrainingRecord;
import com.jcen.medpal.service.TrainingService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/training")
public class TrainingController {

    @Resource
    private TrainingService trainingService;

    @GetMapping("/courses")
    public Object listCourses(@RequestParam(defaultValue = "1") long current,
                              @RequestParam(defaultValue = "10") long size,
                              @RequestParam(required = false) String category) {
        try {
            Page<TrainingCourse> page = new Page<>(current, size);
            IPage<TrainingCourse> result = trainingService.lambdaQuery()
                    .eq(category != null, TrainingCourse::getCategory, category)
                    .eq(TrainingCourse::getStatus, 1)
                    .orderByDesc(TrainingCourse::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/course/{id}")
    public Object getCourse(@PathVariable Long id) {
        try {
            TrainingCourse course = trainingService.getCourseById(id);
            return ResultUtils.success(course);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/record")
    public Object recordLearning(@RequestBody TrainingRecord record) {
        try {
            TrainingRecord result = trainingService.recordLearning(record);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "记录失败");
        }
    }

    @GetMapping("/records")
    public Object getLearningRecords(@RequestParam Long companionId,
                                    @RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size) {
        try {
            var result = trainingService.getLearningRecords(companionId, current, size);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/progress")
    public Object getLearningProgress(@RequestParam Long companionId, @RequestParam Long courseId) {
        try {
            var result = trainingService.getLearningProgress(companionId, courseId);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }
}
