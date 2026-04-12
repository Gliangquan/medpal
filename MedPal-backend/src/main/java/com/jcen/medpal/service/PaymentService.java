package com.jcen.medpal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.Payment;
import com.jcen.medpal.model.vo.PaymentVO;

/**
 * 支付记录Service
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
public interface PaymentService extends IService<Payment> {

    /**
     * 生成支付单号
     *
     * @return 支付单号
     */
    String generatePaymentNo();

    /**
     * 创建支付订单
     *
     * @param orderId   订单ID
     * @param userId    用户ID
     * @param amount    支付金额
     * @param channel   支付渠道
     * @param description 支付描述
     * @return 支付记录VO
     */
    PaymentVO createPayment(Long orderId, Long userId, java.math.BigDecimal amount, String channel, String description);

    /**
     * 模拟支付确认
     *
     * @param paymentId 支付ID
     * @param success   是否支付成功
     * @return 更新后的支付记录VO
     */
    PaymentVO mockPayment(Long paymentId, boolean success);

    /**
     * 查询支付状态
     *
     * @param orderId 订单ID
     * @return 支付记录VO
     */
    PaymentVO getPaymentStatus(Long orderId);

    /**
     * 根据支付ID查询支付记录
     *
     * @param paymentId 支付ID
     * @return 支付记录VO
     */
    PaymentVO getPaymentById(Long paymentId);

    /**
     * 生成支付凭证
     *
     * @param paymentId 支付ID
     * @return 包含凭证信息的VO
     */
    PaymentVO generateVoucher(Long paymentId);
}
