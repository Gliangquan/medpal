package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.ServiceRecordMapper;
import com.jcen.medpal.model.entity.ServiceRecord;
import com.jcen.medpal.service.ServiceRecordService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ServiceRecordServiceImpl extends ServiceImpl<ServiceRecordMapper, ServiceRecord> implements ServiceRecordService {
    
    @Override
    public ServiceRecord createServiceRecord(ServiceRecord serviceRecord) {
        serviceRecord.setCreateTime(LocalDateTime.now());
        this.save(serviceRecord);
        return serviceRecord;
    }
}
