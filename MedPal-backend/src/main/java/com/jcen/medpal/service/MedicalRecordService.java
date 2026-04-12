package com.jcen.medpal.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.MedicalRecord;

public interface MedicalRecordService extends IService<MedicalRecord> {
    
    MedicalRecord createMedicalRecord(MedicalRecord record);
    
    IPage<MedicalRecord> getUserMedicalRecords(Long userId, long current, long size);
    
    String generateRecordNo();
}
