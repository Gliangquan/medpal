package com.jcen.medpal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.UserSettings;

public interface UserSettingsService extends IService<UserSettings> {
    
    UserSettings getUserSettings(Long userId);
    
    boolean updateNotificationSettings(Long userId, UserSettings settings);
    
    boolean updatePrivacySettings(Long userId, UserSettings settings);
    
    UserSettings createDefaultSettings(Long userId);
}
