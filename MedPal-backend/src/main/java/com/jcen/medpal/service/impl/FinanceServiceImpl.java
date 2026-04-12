package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.SettlementMapper;
import com.jcen.medpal.model.entity.Settlement;
import com.jcen.medpal.service.FinanceService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceServiceImpl extends ServiceImpl<SettlementMapper, Settlement> implements FinanceService {
    
    @Override
    public boolean processSettlement(Long id) {
        Settlement settlement = this.getById(id);
        if (settlement == null) {
            return false;
        }
        settlement.setStatus("settled");
        settlement.setSettlementTime(LocalDateTime.now());
        settlement.setUpdateTime(LocalDateTime.now());
        return this.updateById(settlement);
    }
    
    @Override
    public boolean batchProcessSettlements(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            processSettlement(Long.parseLong(id.trim()));
        }
        return true;
    }
    
    @Override
    public Object getFinanceReport(String startDate, String endDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalIncome", 0);
        report.put("totalExpense", 0);
        return report;
    }
    
    @Override
    public Object getFinanceLogs(long current, long size) {
        List<Map<String, Object>> logs = new ArrayList<>();
        return logs;
    }
}
