package com.jcen.medpal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.TrainingCourse;
import com.jcen.medpal.model.entity.TrainingRecord;
import java.util.List;

public interface TrainingService extends IService<TrainingCourse> {
    
    TrainingCourse getCourseById(Long id);
    
    TrainingRecord recordLearning(TrainingRecord record);
    
    List<Object> getLearningRecords(Long companionId, long current, long size);
    
    Object getLearningProgress(Long companionId, Long courseId);
}
