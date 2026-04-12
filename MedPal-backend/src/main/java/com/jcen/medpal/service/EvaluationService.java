package com.jcen.medpal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.Evaluation;

public interface EvaluationService extends IService<Evaluation> {

    Evaluation submitEvaluation(Evaluation evaluation);

    boolean publishEvaluation(Long evaluationId);

    boolean updateCompanionRating(Long companionId);

    /**
     * 获取陪诊员的平均评分
     * @param companionId 陪诊员ID
     * @return 平均评分
     */
    Double getCompanionAverageRating(Long companionId);
}
