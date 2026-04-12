package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.TrainingCourseMapper;
import com.jcen.medpal.model.entity.TrainingCourse;
import com.jcen.medpal.model.entity.TrainingRecord;
import com.jcen.medpal.service.TrainingService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrainingServiceImpl extends ServiceImpl<TrainingCourseMapper, TrainingCourse> implements TrainingService {
    
    @Resource
    private com.jcen.medpal.mapper.TrainingRecordMapper trainingRecordMapper;
    
    @Override
    public TrainingCourse getCourseById(Long id) {
        return this.getById(id);
    }
    
    @Override
    public TrainingRecord recordLearning(TrainingRecord record) {
        record.setCreateTime(LocalDateTime.now());
        trainingRecordMapper.insert(record);
        return record;
    }
    
    @Override
    public List<Object> getLearningRecords(Long companionId, long current, long size) {
        return new ArrayList<>();
    }
    
    @Override
    public Object getLearningProgress(Long companionId, Long courseId) {
        Map<String, Object> progress = new HashMap<>();
        progress.put("companionId", companionId);
        progress.put("courseId", courseId);
        progress.put("progress", 0);
        return progress;
    }
}
