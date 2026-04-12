package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.Department;
import com.jcen.medpal.service.DepartmentService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 科室控制器
 */
@RestController
@RequestMapping("/department")
public class DepartmentController {
    
    @Resource
    private DepartmentService departmentService;
    
    /**
     * 获取科室列表
     */
    @GetMapping("/list")
    public Object listDepartments(@RequestParam(defaultValue = "1") long current,
                                 @RequestParam(defaultValue = "10") long size,
                                 @RequestParam(required = false) Long hospitalId,
                                 @RequestParam(required = false) String keyword) {
        try {
            Page<Department> page = new Page<>(current, size);
            IPage<Department> result = departmentService.lambdaQuery()
                    .eq(Department::getStatus, 1)
                    .eq(hospitalId != null, Department::getHospitalId, hospitalId)
                    .and(keyword != null && !keyword.trim().isEmpty(), wrapper -> wrapper
                            .like(Department::getDepartmentName, keyword.trim())
                            .or()
                            .like(Department::getDepartmentCode, keyword.trim())
                            .or()
                            .like(Department::getIntroduction, keyword.trim()))
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }
    
    /**
     * 获取科室详情
     */
    @GetMapping("/{id}")
    public Object getDepartment(@PathVariable Long id) {
        try {
            Department department = departmentService.getById(id);
            return ResultUtils.success(department);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }
    
    /**
     * 创建科室
     */
    @PostMapping("/create")
    public Object createDepartment(@RequestBody Department department) {
        try {
            department.setStatus(1);
            departmentService.save(department);
            return ResultUtils.success(department);
        } catch (Exception e) {
            return ResultUtils.error(40000, "创建失败");
        }
    }

    @PostMapping("/update")
    public Object updateDepartment(@RequestBody Department department) {
        try {
            if (department == null || department.getId() == null) {
                return ResultUtils.error(40000, "科室ID不能为空");
            }
            boolean result = departmentService.updateById(department);
            return result ? ResultUtils.success(true) : ResultUtils.error(40000, "更新失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "更新失败");
        }
    }

    @PostMapping("/delete/{id}")
    public Object deleteDepartment(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResultUtils.error(40000, "科室ID不能为空");
            }
            boolean result = departmentService.removeById(id);
            return result ? ResultUtils.success(true) : ResultUtils.error(40000, "删除失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "删除失败");
        }
    }
}
