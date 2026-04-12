package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.EmergencyHelpMapper;
import com.jcen.medpal.mapper.UserMapper;
import com.jcen.medpal.model.entity.EmergencyHelp;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.EmergencyHelpService;
import com.jcen.medpal.service.NotificationService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.Resource;

@Service
public class EmergencyHelpServiceImpl extends ServiceImpl<EmergencyHelpMapper, EmergencyHelp> implements EmergencyHelpService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private NotificationService notificationService;
    
    @Override
    public EmergencyHelp createEmergencyHelp(EmergencyHelp emergencyHelp) {
        emergencyHelp.setStatus("pending");
        emergencyHelp.setCreateTime(LocalDateTime.now());
        emergencyHelp.setUpdateTime(LocalDateTime.now());
        this.save(emergencyHelp);
        notifyApprovedCompanions(emergencyHelp);
        return emergencyHelp;
    }

    private void notifyApprovedCompanions(EmergencyHelp emergencyHelp) {
        List<User> companions = userMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("user_role", "companion")
                .eq("status", 1)
                .eq("real_name_status", "approved")
                .eq("qualification_status", "approved"));
        for (User companion : companions) {
            notificationService.createNotification(
                    companion.getId(),
                    "emergency",
                    "收到紧急求助",
                    "附近有用户发起紧急求助，请尽快查看并联系处理",
                    "emergency",
                    emergencyHelp.getId());
        }
    }
    
    @Override
    public boolean respondEmergencyHelp(Long id, Long responderId) {
        EmergencyHelp help = this.getById(id);
        if (help == null) {
            return false;
        }
        help.setStatus("responded");
        help.setResponderId(responderId);
        help.setUpdateTime(LocalDateTime.now());
        return this.updateById(help);
    }
    
    @Override
    public boolean resolveEmergencyHelp(Long id) {
        EmergencyHelp help = this.getById(id);
        if (help == null) {
            return false;
        }
        help.setStatus("resolved");
        help.setResolveTime(LocalDateTime.now());
        help.setUpdateTime(LocalDateTime.now());
        return this.updateById(help);
    }
}
