package com.jcen.medpal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.Hospital;
import com.jcen.medpal.model.vo.HospitalNearbyVO;
import java.math.BigDecimal;
import java.util.List;

public interface HospitalService extends IService<Hospital> {

    /**
     * 获取附近医院（按距离排序）
     *
     * @param latitude  用户纬度
     * @param longitude 用户经度
     * @param radius    搜索半径（公里）
     * @param limit     返回结果数量限制
     * @return 附近医院列表（按距离升序）
     */
    List<HospitalNearbyVO> getNearbyHospitals(BigDecimal latitude, BigDecimal longitude, int radius, int limit);

    List<Hospital> searchHospitals(String keyword);
}
