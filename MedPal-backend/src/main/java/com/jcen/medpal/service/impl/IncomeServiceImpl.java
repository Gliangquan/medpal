package com.jcen.medpal.service.impl;

import com.jcen.medpal.service.IncomeService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IncomeServiceImpl implements IncomeService {
    
    @Override
    public Object getIncomeDetail(Long companionId, String startDate, String endDate) {
        List<Map<String, Object>> details = new ArrayList<>();
        return details;
    }
    
    @Override
    public Object getIncomeStatistics(Long companionId, String type) {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("companionId", companionId);
        statistics.put("type", type);
        return statistics;
    }
    
    @Override
    public Object getTotalIncome(Long companionId) {
        Map<String, Object> total = new HashMap<>();
        total.put("companionId", companionId);
        total.put("amount", 0);
        return total;
    }
    
    @Override
    public Object getSettlementRule() {
        Map<String, Object> rule = new HashMap<>();
        rule.put("settlementCycle", "月结");
        rule.put("serviceFeeRate", 0.1);
        return rule;
    }
}
