package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.AppointmentOrder;
import com.jcen.medpal.model.entity.Evaluation;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.AppointmentOrderService;
import com.jcen.medpal.service.EvaluationService;
import com.jcen.medpal.service.UserService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/evaluation")
public class EvaluationController {
    
    @Resource
    private EvaluationService evaluationService;

    @Resource
    private UserService userService;

    @Resource
    private AppointmentOrderService appointmentOrderService;
    
    /**
     * 创建评价
     */
    @PostMapping("/create")
    public Object createEvaluation(@RequestBody Evaluation evaluation, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            if (!"patient".equals(loginUser.getUserRole()) && !"user".equals(loginUser.getUserRole())) {
                return ResultUtils.error(40300, "仅患者可评价");
            }
            AppointmentOrder order = appointmentOrderService.getById(evaluation.getOrderId());
            if (order == null) {
                return ResultUtils.error(40400, "订单不存在");
            }
            if (order.getUserId() == null || !order.getUserId().equals(loginUser.getId())) {
                return ResultUtils.error(40300, "仅可评价自己的订单");
            }
            if (!"completed".equals(order.getOrderStatus())) {
                return ResultUtils.error(40000, "订单未完成，暂不可评价");
            }
            evaluation.setUserId(loginUser.getId());
            if (order.getCompanionId() != null) {
                evaluation.setCompanionId(order.getCompanionId());
            }
            Evaluation savedEvaluation = evaluationService.submitEvaluation(evaluation);
            return ResultUtils.success(savedEvaluation);
        } catch (Exception e) {
            return ResultUtils.error(40000, "创建评价失败");
        }
    }
    
    /**
     * 获取评价列表
     */
    @GetMapping("/list")
    public Object listEvaluations(@RequestParam(defaultValue = "1") long current,
                                 @RequestParam(defaultValue = "10") long size,
                                 @RequestParam(required = false) Long orderId,
                                 @RequestParam(required = false) Long companionId,
                                 @RequestParam(required = false) Long userId) {
        try {
            Page<Evaluation> page = new Page<>(current, size);
            IPage<Evaluation> result = evaluationService.lambdaQuery()
                    .eq(orderId != null, Evaluation::getOrderId, orderId)
                    .eq(companionId != null, Evaluation::getCompanionId, companionId)
                    .eq(userId != null, Evaluation::getUserId, userId)
                    .orderByDesc(Evaluation::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }
    
    /**
     * 获取评价详情
     */
    @GetMapping("/{id}")
    public Object getEvaluation(@PathVariable Long id) {
        try {
            Evaluation evaluation = evaluationService.getById(id);
            return ResultUtils.success(evaluation);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }
    
    /**
     * 获取陪诊员的平均评分
     */
    @GetMapping("/companion/{companionId}/rating")
    public Object getCompanionRating(@PathVariable Long companionId) {
        try {
            Double rating = evaluationService.getCompanionAverageRating(companionId);
            return ResultUtils.success(rating);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    /**
     * 修改评价（仅限未公开的评价）
     */
    @PostMapping("/update/{id}")
    public Object updateEvaluation(@PathVariable Long id, @RequestBody Evaluation evaluation) {
        try {
            Evaluation existing = evaluationService.getById(id);
            if (existing == null) {
                return ResultUtils.error(40400, "评价不存在");
            }
            if (!"pending".equals(existing.getStatus())) {
                return ResultUtils.error(40000, "已公开的评价不能修改");
            }
            evaluation.setId(id);
            evaluation.setUpdateTime(java.time.LocalDateTime.now());
            boolean result = evaluationService.updateById(evaluation);
            return result ? ResultUtils.success("更新成功") : ResultUtils.error(40000, "更新失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "更新失败");
        }
    }

    /**
     * 删除评价（仅限未公开的评价）
     */
    @PostMapping("/delete/{id}")
    public Object deleteEvaluation(@PathVariable Long id) {
        try {
            Evaluation existing = evaluationService.getById(id);
            if (existing == null) {
                return ResultUtils.error(40400, "评价不存在");
            }
            if (!"pending".equals(existing.getStatus())) {
                return ResultUtils.error(40000, "已公开的评价不能删除");
            }
            boolean result = evaluationService.removeById(id);
            return result ? ResultUtils.success("删除成功") : ResultUtils.error(40000, "删除失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "删除失败");
        }
    }
}
