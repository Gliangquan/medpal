package com.jcen.medpal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.Settlement;

public interface FinanceService extends IService<Settlement> {
    
    boolean processSettlement(Long id);
    
    boolean batchProcessSettlements(String ids);
    
    Object getFinanceReport(String startDate, String endDate);
    
    Object getFinanceLogs(long current, long size);
}
