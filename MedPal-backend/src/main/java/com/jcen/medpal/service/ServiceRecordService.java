package com.jcen.medpal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.ServiceRecord;

public interface ServiceRecordService extends IService<ServiceRecord> {
    
    ServiceRecord createServiceRecord(ServiceRecord serviceRecord);
}
