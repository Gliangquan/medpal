package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.AppointmentOrderMapper;
import com.jcen.medpal.mapper.MedicalRecordMapper;
import com.jcen.medpal.model.entity.MedicalRecord;
import com.jcen.medpal.service.MedicalRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MedicalRecordServiceImpl extends ServiceImpl<MedicalRecordMapper, MedicalRecord> implements MedicalRecordService {

    @Resource
    private AppointmentOrderMapper appointmentOrderMapper;
    
    @Override
    public MedicalRecord createMedicalRecord(MedicalRecord record) {
        record.setRecordNo(generateRecordNo());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setIsDelete(0);
        this.save(record);
        return record;
    }
    
    @Override
    public IPage<MedicalRecord> getUserMedicalRecords(Long userId, long current, long size) {
        Page<MedicalRecord> page = new Page<>(current, size);
        return this.lambdaQuery()
                .eq(MedicalRecord::getUserId, userId)
                .eq(MedicalRecord::getIsDelete, 0)
                .orderByDesc(MedicalRecord::getVisitDate)
                .page(page);
    }
    
    @Override
    public boolean hasAccessibleOrderRelation(Long userId, Long companionId) {
        if (userId == null || companionId == null) {
            return false;
        }
        return appointmentOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.jcen.medpal.model.entity.AppointmentOrder>()
                        .eq("user_id", userId)
                        .eq("companion_id", companionId)
                        .in("order_status", "confirmed", "serving", "completion_pending", "completed")
                        .eq("payment_status", "paid")
        ) > 0;
    }

    @Override
    public String generateRecordNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "MR" + timestamp + random;
    }
}
