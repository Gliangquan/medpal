package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.Doctor;
import com.jcen.medpal.service.DoctorService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 医生控制器
 */
@RestController
@RequestMapping("/doctor")
public class DoctorController {
    
    @Resource
    private DoctorService doctorService;
    
    /**
     * 获取医生列表
     */
    @GetMapping("/list")
    public Object listDoctors(@RequestParam(defaultValue = "1") long current,
                             @RequestParam(defaultValue = "10") long size,
                             @RequestParam(required = false) Long departmentId,
                             @RequestParam(required = false) Long hospitalId,
                             @RequestParam(required = false) String keyword) {
        try {
            Page<Doctor> page = new Page<>(current, size);
            IPage<Doctor> result = doctorService.lambdaQuery()
                    .eq(Doctor::getStatus, 1)
                    .eq(departmentId != null, Doctor::getDepartmentId, departmentId)
                    .eq(hospitalId != null, Doctor::getHospitalId, hospitalId)
                    .and(keyword != null && !keyword.trim().isEmpty(), wrapper -> wrapper
                            .like(Doctor::getDoctorName, keyword.trim())
                            .or()
                            .like(Doctor::getDoctorTitle, keyword.trim())
                            .or()
                            .like(Doctor::getSpecialties, keyword.trim())
                            .or()
                            .like(Doctor::getIntroduction, keyword.trim())
                            .or()
                            .like(Doctor::getClinicTime, keyword.trim()))
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }
    
    /**
     * 获取医生详情
     */
    @GetMapping("/{id}")
    public Object getDoctor(@PathVariable Long id) {
        try {
            Doctor doctor = doctorService.getById(id);
            return ResultUtils.success(doctor);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }
    
    /**
     * 创建医生
     */
    @PostMapping("/create")
    public Object createDoctor(@RequestBody Doctor doctor) {
        try {
            doctor.setStatus(1);
            doctorService.save(doctor);
            return ResultUtils.success(doctor);
        } catch (Exception e) {
            return ResultUtils.error(40000, "创建失败");
        }
    }

    @PostMapping("/update")
    public Object updateDoctor(@RequestBody Doctor doctor) {
        try {
            if (doctor == null || doctor.getId() == null) {
                return ResultUtils.error(40000, "医生ID不能为空");
            }
            boolean result = doctorService.updateById(doctor);
            return result ? ResultUtils.success(true) : ResultUtils.error(40000, "更新失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "更新失败");
        }
    }

    @PostMapping("/delete/{id}")
    public Object deleteDoctor(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResultUtils.error(40000, "医生ID不能为空");
            }
            boolean result = doctorService.removeById(id);
            return result ? ResultUtils.success(true) : ResultUtils.error(40000, "删除失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "删除失败");
        }
    }
}
