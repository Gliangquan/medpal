package com.jcen.medpal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.EmergencyHelp;

public interface EmergencyHelpService extends IService<EmergencyHelp> {
    
    EmergencyHelp createEmergencyHelp(EmergencyHelp emergencyHelp);
    
    boolean respondEmergencyHelp(Long id, Long responderId);
    
    boolean resolveEmergencyHelp(Long id);
}
