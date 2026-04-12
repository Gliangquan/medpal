package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.EvaluationMapper;
import com.jcen.medpal.mapper.UserMapper;
import com.jcen.medpal.model.entity.Evaluation;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.EvaluationService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.annotation.Resource;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    @Resource
    private UserMapper userMapper;
    
    @Override
    public Evaluation submitEvaluation(Evaluation evaluation) {
        if (evaluation.getProfessionalismScore() != null && 
            evaluation.getAttitudeScore() != null && 
            evaluation.getEfficiencyScore() != null && 
            evaluation.getSatisfactionScore() != null) {
            
            int total = evaluation.getProfessionalismScore() + 
                       evaluation.getAttitudeScore() + 
                       evaluation.getEfficiencyScore() + 
                       evaluation.getSatisfactionScore();
            BigDecimal avg = new BigDecimal(total).divide(new BigDecimal(4), 2, RoundingMode.HALF_UP);
            evaluation.setAverageScore(avg);
        }
        
        if (evaluation.getStatus() == null || evaluation.getStatus().trim().isEmpty()) {
            evaluation.setStatus("published");
        }
        evaluation.setCreateTime(LocalDateTime.now());
        evaluation.setUpdateTime(LocalDateTime.now());
        this.save(evaluation);
        updateCompanionRating(evaluation.getCompanionId());
        return evaluation;
    }
    
    @Override
    public boolean publishEvaluation(Long evaluationId) {
        Evaluation evaluation = this.getById(evaluationId);
        if (evaluation == null) {
            return false;
        }
        evaluation.setStatus("published");
        evaluation.setUpdateTime(LocalDateTime.now());
        return this.updateById(evaluation);
    }
    
    @Override
    public boolean updateCompanionRating(Long companionId) {
        if (companionId == null) {
            return false;
        }
        User companion = userMapper.selectById(companionId);
        if (companion == null) {
            return false;
        }
        Double averageRating = getCompanionAverageRating(companionId);
        BigDecimal normalizedRating = BigDecimal.valueOf(averageRating == null ? 0D : averageRating)
                .setScale(2, RoundingMode.HALF_UP);
        companion.setRating(normalizedRating.doubleValue());
        companion.setUpdateTime(new java.util.Date());
        return userMapper.updateById(companion) > 0;
    }

    @Override
    public Double getCompanionAverageRating(Long companionId) {
        return this.lambdaQuery()
                .eq(Evaluation::getCompanionId, companionId)
                .ne(Evaluation::getStatus, "pending")
                .list()
                .stream()
                .mapToDouble(e -> e.getAverageScore() != null ? e.getAverageScore().doubleValue() : 0.0)
                .average()
                .orElse(0.0);
    }
}
