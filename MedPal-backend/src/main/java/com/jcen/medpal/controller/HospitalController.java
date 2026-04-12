package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.dto.HospitalSearchRequest;
import com.jcen.medpal.model.entity.Hospital;
import com.jcen.medpal.service.HospitalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 医院接口
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@RestController
@RequestMapping("/hospital")
@Slf4j
public class HospitalController {
    
    @Resource
    private HospitalService hospitalService;
    
    /**
     * 获取医院列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @return 医院列表
     */
    @GetMapping("/list")
    public Object listHospitals(@RequestParam(defaultValue = "1") long current,
                               @RequestParam(defaultValue = "10") long size) {
        try {
            Page<Hospital> page = new Page<>(current, size);
            IPage<Hospital> result = hospitalService.lambdaQuery()
                    .eq(Hospital::getStatus, 1)
                    .orderByDesc(Hospital::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("查询医院列表失败", e);
            return ResultUtils.error(40000, "查询医院列表失败");
        }
    }
    
    /**
     * 获取医院详情
     *
     * @param id 医院ID
     * @return 医院详情
     */
    @GetMapping("/{id}")
    public Object getHospital(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResultUtils.error(40000, "医院ID不能为空");
            }
            Hospital hospital = hospitalService.getById(id);
            if (hospital == null) {
                return ResultUtils.error(40000, "医院不存在");
            }
            return ResultUtils.success(hospital);
        } catch (Exception e) {
            log.error("查询医院详情失败", e);
            return ResultUtils.error(40000, "查询医院详情失败");
        }
    }
    
    /**
     * 搜索医院
     *
     * @param searchRequest 搜索请求（包含关键词）
     * @return 搜索结果
     */
    @PostMapping("/search")
    public Object searchHospitals(@RequestBody HospitalSearchRequest searchRequest) {
        try {
            if (searchRequest == null || searchRequest.getKeyword() == null || searchRequest.getKeyword().isEmpty()) {
                return ResultUtils.error(40000, "搜索关键词不能为空");
            }
            var result = hospitalService.searchHospitals(searchRequest.getKeyword());
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("搜索医院失败", e);
            return ResultUtils.error(40000, "搜索医院失败");
        }
    }
    
    /**
     * 获取附近医院
     * 基于经纬度和半径范围，使用Haversine公式计算实际距离
     *
     * @param latitude  用户纬度
     * @param longitude 用户经度
     * @param radius    搜索半径（单位：km，默认5km）
     * @param limit     返回结果数量（默认20）
     * @return 附近医院列表（按距离升序，包含distance字段）
     */
    @GetMapping("/nearby")
    public Object getNearbyHospitals(@RequestParam BigDecimal latitude,
                                     @RequestParam BigDecimal longitude,
                                     @RequestParam(defaultValue = "5") int radius,
                                     @RequestParam(defaultValue = "20") int limit) {
        try {
            if (latitude == null || longitude == null) {
                return ResultUtils.error(40000, "经纬度不能为空");
            }
            if (radius <= 0) {
                return ResultUtils.error(40000, "搜索半径必须大于0");
            }
            if (limit <= 0 || limit > 100) {
                return ResultUtils.error(40000, "返回数量必须在1-100之间");
            }
            var result = hospitalService.getNearbyHospitals(latitude, longitude, radius, limit);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("查询附近医院失败", e);
            return ResultUtils.error(40000, "查询附近医院失败");
        }
    }
    
    /**
     * 创建医院（管理员）
     *
     * @param hospital 医院信息
     * @return 创建结果
     */
    @PostMapping("/create")
    public Object createHospital(@RequestBody Hospital hospital) {
        try {
            if (hospital == null) {
                return ResultUtils.error(40000, "医院信息不能为空");
            }
            if (hospital.getHospitalName() == null || hospital.getHospitalName().isEmpty()) {
                return ResultUtils.error(40000, "医院名称不能为空");
            }
            hospital.setStatus(1);
            hospitalService.save(hospital);
            return ResultUtils.success(hospital);
        } catch (Exception e) {
            log.error("创建医院失败", e);
            return ResultUtils.error(40000, "创建医院失败");
        }
    }
    
    /**
     * 更新医院信息
     *
     * @param hospital 医院信息
     * @return 更新结果
     */
    @PostMapping("/update")
    public Object updateHospital(@RequestBody Hospital hospital) {
        try {
            if (hospital == null || hospital.getId() == null) {
                return ResultUtils.error(40000, "医院ID不能为空");
            }
            hospitalService.updateById(hospital);
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("更新医院信息失败", e);
            return ResultUtils.error(40000, "更新医院信息失败");
        }
    }
    
    /**
     * 删除医院
     *
     * @param id 医院ID
     * @return 删除结果
     */
    @PostMapping("/delete/{id}")
    public Object deleteHospital(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResultUtils.error(40000, "医院ID不能为空");
            }
            hospitalService.removeById(id);
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("删除医院失败", e);
            return ResultUtils.error(40000, "删除医院失败");
        }
    }
}
