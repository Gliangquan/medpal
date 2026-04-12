package com.jcen.medpal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcen.medpal.model.entity.Hospital;
import com.jcen.medpal.model.vo.HospitalNearbyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface HospitalMapper extends BaseMapper<Hospital> {

    /**
     * 查询附近医院（使用Haversine公式计算距离）
     *
     * @param latitude  用户纬度
     * @param longitude 用户经度
     * @param radius    搜索半径（公里）
     * @param limit     返回结果数量限制
     * @return 附近医院列表，包含距离信息
     */
    List<HospitalNearbyVO> selectNearbyHospitals(@Param("latitude") BigDecimal latitude,
                                                 @Param("longitude") BigDecimal longitude,
                                                 @Param("radius") int radius,
                                                 @Param("limit") int limit);
}
