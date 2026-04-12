package com.jcen.medpal.service;

public interface StatisticsService {
    
    Object getDashboardData();
    
    Object getUserStatistics(String startDate, String endDate);
    
    Object getOrderStatistics(String startDate, String endDate);
    
    Object getCompanionStatistics(String startDate, String endDate);
    
    Object getRevenueStatistics(String startDate, String endDate);
}
