package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.annotation.AuthCheck;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.constant.UserConstant;
import com.jcen.medpal.model.entity.TrainingCourse;
import com.jcen.medpal.service.TrainingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/training/admin")
public class TrainingAdminController {

    @Resource
    private TrainingService trainingService;

    @GetMapping("/courses")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object listCourses(@RequestParam(defaultValue = "1") long current,
                              @RequestParam(defaultValue = "10") long size,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer status,
                              @RequestParam(required = false) String category) {
        try {
            Page<TrainingCourse> page = new Page<>(current, size);
            IPage<TrainingCourse> result = trainingService.lambdaQuery()
                    .like(StringUtils.isNotBlank(keyword), TrainingCourse::getTitle, keyword)
                    .eq(status != null, TrainingCourse::getStatus, status)
                    .eq(StringUtils.isNotBlank(category), TrainingCourse::getCategory, category)
                    .eq(TrainingCourse::getIsDelete, 0)
                    .orderByDesc(TrainingCourse::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    @PostMapping("/course")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object createCourse(@RequestBody TrainingCourse course) {
        try {
            if (course == null || StringUtils.isBlank(course.getTitle()) || StringUtils.isBlank(course.getContent())) {
                return ResultUtils.error(40000, "课程标题和内容不能为空");
            }
            course.setId(null);
            course.setStatus(course.getStatus() == null ? 1 : course.getStatus());
            course.setIsDelete(0);
            course.setCreateTime(LocalDateTime.now());
            course.setUpdateTime(LocalDateTime.now());
            boolean result = trainingService.save(course);
            return result ? ResultUtils.success(course) : ResultUtils.error(40000, "创建失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "创建失败" : e.getMessage());
        }
    }

    @PostMapping("/course/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object updateCourse(@PathVariable Long id, @RequestBody TrainingCourse course) {
        try {
            TrainingCourse existed = trainingService.getById(id);
            if (existed == null || existed.getIsDelete() != null && existed.getIsDelete() == 1) {
                return ResultUtils.error(40000, "课程不存在");
            }
            existed.setTitle(StringUtils.defaultIfBlank(course.getTitle(), existed.getTitle()));
            existed.setCategory(course.getCategory());
            existed.setDescription(course.getDescription());
            existed.setContent(StringUtils.defaultIfBlank(course.getContent(), existed.getContent()));
            existed.setDifficulty(course.getDifficulty());
            existed.setDuration(course.getDuration());
            existed.setInstructor(course.getInstructor());
            if (course.getStatus() != null) {
                existed.setStatus(course.getStatus());
            }
            existed.setUpdateTime(LocalDateTime.now());
            boolean result = trainingService.updateById(existed);
            return result ? ResultUtils.success("更新成功") : ResultUtils.error(40000, "更新失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "更新失败" : e.getMessage());
        }
    }

    @PostMapping("/course/{id}/publish")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object publishCourse(@PathVariable Long id) {
        try {
            TrainingCourse existed = trainingService.getById(id);
            if (existed == null) {
                return ResultUtils.error(40000, "课程不存在");
            }
            existed.setStatus(1);
            existed.setUpdateTime(LocalDateTime.now());
            boolean result = trainingService.updateById(existed);
            return result ? ResultUtils.success("发布成功") : ResultUtils.error(40000, "发布失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "发布失败" : e.getMessage());
        }
    }

    @PostMapping("/course/{id}/unpublish")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object unpublishCourse(@PathVariable Long id) {
        try {
            TrainingCourse existed = trainingService.getById(id);
            if (existed == null) {
                return ResultUtils.error(40000, "课程不存在");
            }
            existed.setStatus(0);
            existed.setUpdateTime(LocalDateTime.now());
            boolean result = trainingService.updateById(existed);
            return result ? ResultUtils.success("下架成功") : ResultUtils.error(40000, "下架失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "下架失败" : e.getMessage());
        }
    }

    @PostMapping("/course/{id}/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object deleteCourse(@PathVariable Long id) {
        try {
            TrainingCourse existed = trainingService.getById(id);
            if (existed == null) {
                return ResultUtils.error(40000, "课程不存在");
            }
            existed.setIsDelete(1);
            existed.setUpdateTime(LocalDateTime.now());
            boolean result = trainingService.updateById(existed);
            return result ? ResultUtils.success("删除成功") : ResultUtils.error(40000, "删除失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, e.getMessage() == null ? "删除失败" : e.getMessage());
        }
    }
}
