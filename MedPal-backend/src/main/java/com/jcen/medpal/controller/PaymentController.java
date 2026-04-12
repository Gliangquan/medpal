package com.jcen.medpal.controller;

import com.jcen.medpal.common.BaseResponse;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.exception.BusinessException;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.vo.PaymentVO;
import com.jcen.medpal.service.PaymentService;
import com.jcen.medpal.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * 支付接口
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    /**
     * 创建支付订单
     *
     * @param orderId     订单ID
     * @param amount      支付金额
     * @param channel     支付渠道（可选，默认为mock）
     * @param description 支付描述（可选）
     * @param request     HTTP请求
     * @return 支付记录VO
     */
    @PostMapping("/create")
    public BaseResponse<PaymentVO> createPayment(
            @RequestParam Long orderId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "mock") String channel,
            @RequestParam(required = false) String description,
            HttpServletRequest request) {
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID不能为空");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付金额必须大于0");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        PaymentVO paymentVO = paymentService.createPayment(orderId, loginUser.getId(), amount, channel, description);
        return ResultUtils.success(paymentVO);
    }

    /**
     * 模拟支付确认
     *
     * @param paymentId 支付ID
     * @param success   是否支付成功（可选，默认为true）
     * @return 更新后的支付记录VO
     */
    @PostMapping("/mock-pay")
    public BaseResponse<PaymentVO> mockPayment(
            @RequestParam Long paymentId,
            @RequestParam(required = false, defaultValue = "true") boolean success) {
        if (paymentId == null || paymentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付ID不能为空");
        }

        PaymentVO paymentVO = paymentService.mockPayment(paymentId, success);
        return ResultUtils.success(paymentVO);
    }

    /**
     * 查询支付状态
     *
     * @param orderId 订单ID
     * @return 支付记录VO
     */
    @GetMapping("/status/{orderId}")
    public BaseResponse<PaymentVO> getPaymentStatus(@PathVariable Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID不能为空");
        }

        PaymentVO paymentVO = paymentService.getPaymentStatus(orderId);
        if (paymentVO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到支付记录");
        }
        return ResultUtils.success(paymentVO);
    }

    /**
     * 根据支付ID查询支付详情
     *
     * @param paymentId 支付ID
     * @return 支付记录VO
     */
    @GetMapping("/detail/{paymentId}")
    public BaseResponse<PaymentVO> getPaymentDetail(@PathVariable Long paymentId) {
        if (paymentId == null || paymentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付ID不能为空");
        }

        PaymentVO paymentVO = paymentService.getPaymentById(paymentId);
        if (paymentVO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "支付记录不存在");
        }
        return ResultUtils.success(paymentVO);
    }

    /**
     * 获取支付凭证
     *
     * @param paymentId 支付ID
     * @return 包含凭证信息的支付记录VO
     */
    @GetMapping("/voucher/{paymentId}")
    public BaseResponse<PaymentVO> getPaymentVoucher(@PathVariable Long paymentId) {
        if (paymentId == null || paymentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付ID不能为空");
        }

        PaymentVO paymentVO = paymentService.generateVoucher(paymentId);
        return ResultUtils.success(paymentVO);
    }
}
