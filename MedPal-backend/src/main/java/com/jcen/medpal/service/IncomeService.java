package com.jcen.medpal.service;

public interface IncomeService {
    
    Object getIncomeDetail(Long companionId, String startDate, String endDate);
    
    Object getIncomeStatistics(Long companionId, String type);
    
    Object getTotalIncome(Long companionId);
    
    Object getSettlementRule();
}
