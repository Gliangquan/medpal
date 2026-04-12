package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.UserSettingsMapper;
import com.jcen.medpal.model.entity.UserSettings;
import com.jcen.medpal.service.UserSettingsService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserSettingsServiceImpl extends ServiceImpl<UserSettingsMapper, UserSettings> implements UserSettingsService {
    
    @Override
    public UserSettings getUserSettings(Long userId) {
        UserSettings settings = this.lambdaQuery()
                .eq(UserSettings::getUserId, userId)
                .eq(UserSettings::getIsDelete, 0)
                .one();
        
        if (settings == null) {
            settings = createDefaultSettings(userId);
        }
        
        return settings;
    }
    
    @Override
    public boolean updateNotificationSettings(Long userId, UserSettings settings) {
        UserSettings existing = getUserSettings(userId);
        existing.setOrderNotificationEnabled(settings.getOrderNotificationEnabled());
        existing.setActivityNotificationEnabled(settings.getActivityNotificationEnabled());
        existing.setHealthPushEnabled(settings.getHealthPushEnabled());
        existing.setChatNotificationEnabled(settings.getChatNotificationEnabled());
        existing.setUpdateTime(LocalDateTime.now());
        return this.updateById(existing);
    }
    
    @Override
    public boolean updatePrivacySettings(Long userId, UserSettings settings) {
        UserSettings existing = getUserSettings(userId);
        existing.setMedicalRecordVisible(settings.getMedicalRecordVisible());
        existing.setProfileVisible(settings.getProfileVisible());
        existing.setChatHistorySaveDays(settings.getChatHistorySaveDays());
        existing.setUpdateTime(LocalDateTime.now());
        return this.updateById(existing);
    }
    
    @Override
    public UserSettings createDefaultSettings(Long userId) {
        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        
        // 默认通知设置 - 全部开启
        settings.setOrderNotificationEnabled(1);
        settings.setActivityNotificationEnabled(1);
        settings.setHealthPushEnabled(1);
        settings.setChatNotificationEnabled(1);
        
        // 默认隐私设置
        settings.setMedicalRecordVisible(0); // 默认不公开
        settings.setProfileVisible(1); // 默认公开
        settings.setChatHistorySaveDays(30); // 默认保存30天
        
        settings.setCreateTime(LocalDateTime.now());
        settings.setUpdateTime(LocalDateTime.now());
        settings.setIsDelete(0);
        
        this.save(settings);
        return settings;
    }
}
