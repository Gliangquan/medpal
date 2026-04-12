package com.jcen.medpal.service;

public interface RecommendationService {

    Object recommendServices(Long userId);

    Object recommendHospitals(Long userId);

    Object getTopCompanions(int limit);

    Object getCompanionsBySpecialty(String specialty);

    Object getGoldCompanions(int limit);
}
