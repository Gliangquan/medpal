package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.HospitalMapper;
import com.jcen.medpal.model.entity.Hospital;
import com.jcen.medpal.model.vo.HospitalNearbyVO;
import com.jcen.medpal.service.HospitalService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class HospitalServiceImpl extends ServiceImpl<HospitalMapper, Hospital> implements HospitalService {

    @Override
    public List<HospitalNearbyVO> getNearbyHospitals(BigDecimal latitude, BigDecimal longitude, int radius, int limit) {
        return baseMapper.selectNearbyHospitals(latitude, longitude, radius, limit);
    }

    @Override
    public List<Hospital> searchHospitals(String keyword) {
        return this.lambdaQuery()
                .like(Hospital::getHospitalName, keyword)
                .eq(Hospital::getStatus, 1)
                .list();
    }
}
