package com.jcen.medpal.service.impl;

import com.jcen.medpal.service.RecommendationService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    
    @Override
    public Object recommendServices(Long userId) {
        List<Map<String, Object>> services = new ArrayList<>();
        Map<String, Object> service1 = new HashMap<>();
        service1.put("id", 1);
        service1.put("name", "老年专属陪诊");
        service1.put("price", 200);
        services.add(service1);
        return services;
    }
    
    @Override
    public Object recommendHospitals(Long userId) {
        List<Map<String, Object>> hospitals = new ArrayList<>();
        return hospitals;
    }
    
    @Override
    public Object getTopCompanions(int limit) {
        List<Map<String, Object>> companions = new ArrayList<>();
        return companions;
    }
    
    @Override
    public Object getCompanionsBySpecialty(String specialty) {
        List<Map<String, Object>> companions = new ArrayList<>();
        return companions;
    }

    @Override
    public Object getGoldCompanions(int limit) {
        List<Map<String, Object>> goldCompanions = new ArrayList<>();
        return goldCompanions;
    }
}
