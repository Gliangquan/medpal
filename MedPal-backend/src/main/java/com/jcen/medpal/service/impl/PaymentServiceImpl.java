package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.exception.BusinessException;
import com.jcen.medpal.mapper.AppointmentOrderMapper;
import com.jcen.medpal.mapper.PaymentMapper;
import com.jcen.medpal.model.entity.AppointmentOrder;
import com.jcen.medpal.model.entity.Payment;
import com.jcen.medpal.model.vo.PaymentVO;
import com.jcen.medpal.service.AppointmentOrderService;
import com.jcen.medpal.service.PaymentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Autowired
    private AppointmentOrderService appointmentOrderService;

    @Autowired
    private AppointmentOrderMapper appointmentOrderMapper;

    @Override
    public String generatePaymentNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = (int)(Math.random() * 10000);
        return "PAY" + timestamp + String.format("%04d", random);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO createPayment(Long orderId, Long userId, BigDecimal amount, String channel, String description) {
        // 查询订单是否存在
        AppointmentOrder order = appointmentOrderService.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        // 检查订单是否已支付
        if ("paid".equals(order.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED_ERROR, "订单已支付，请勿重复支付");
        }

        // 检查订单是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作该订单");
        }

        // 检查金额是否匹配
        if (amount.compareTo(order.getTotalPrice()) != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付金额与订单金额不匹配");
        }

        // 检查是否有未完成的支付记录
        Payment existingPayment = this.baseMapper.selectByOrderId(orderId);
        if (existingPayment != null && ("unpaid".equals(existingPayment.getPaymentStatus()) || "processing".equals(existingPayment.getPaymentStatus()))) {
            // 返回已有的未支付记录
            return convertToVO(existingPayment);
        }

        // 创建新的支付记录
        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderId(orderId);
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setPaymentChannel(channel != null ? channel : "mock");
        payment.setPaymentStatus("unpaid");
        payment.setDescription(description);
        payment.setExpireTime(LocalDateTime.now().plusHours(24)); // 默认24小时过期
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        payment.setIsDelete(0);

        this.save(payment);

        // 更新订单支付状态为处理中
        order.setPaymentStatus("processing");
        order.setUpdateTime(LocalDateTime.now());
        appointmentOrderMapper.updateById(order);

        return convertToVO(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO mockPayment(Long paymentId, boolean success) {
        Payment payment = this.getById(paymentId);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "支付记录不存在");
        }

        // 检查支付状态
        if (!"unpaid".equals(payment.getPaymentStatus()) && !"processing".equals(payment.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED_ERROR, "当前支付状态不允许操作");
        }

        // 检查是否过期
        if (payment.getExpireTime() != null && payment.getExpireTime().isBefore(LocalDateTime.now())) {
            payment.setPaymentStatus("failed");
            payment.setUpdateTime(LocalDateTime.now());
            this.updateById(payment);
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED_ERROR, "支付已过期");
        }

        if (success) {
            // 支付成功
            payment.setPaymentStatus("paid");
            payment.setPaidTime(LocalDateTime.now());
            payment.setTransactionId("MOCK_" + System.currentTimeMillis());
            payment.setVoucherNo("VCH" + payment.getPaymentNo().substring(3));
            payment.setUpdateTime(LocalDateTime.now());
            this.updateById(payment);

            // 更新订单状态
            AppointmentOrder order = appointmentOrderService.getById(payment.getOrderId());
            if (order != null) {
                order.setPaymentStatus("paid");
                // 支付仅影响支付状态；接单状态仍由陪诊员接单动作驱动
                if (order.getOrderStatus() == null || order.getOrderStatus().isEmpty()) {
                    order.setOrderStatus("pending");
                }
                order.setUpdateTime(LocalDateTime.now());
                appointmentOrderMapper.updateById(order);
            }
        } else {
            // 支付失败
            payment.setPaymentStatus("failed");
            payment.setUpdateTime(LocalDateTime.now());
            this.updateById(payment);

            // 更新订单支付状态
            AppointmentOrder order = appointmentOrderService.getById(payment.getOrderId());
            if (order != null) {
                order.setPaymentStatus("unpaid");
                order.setUpdateTime(LocalDateTime.now());
                appointmentOrderMapper.updateById(order);
            }
        }

        return convertToVO(payment);
    }

    @Override
    public PaymentVO getPaymentStatus(Long orderId) {
        Payment payment = this.baseMapper.selectByOrderId(orderId);
        if (payment == null) {
            return null;
        }
        return convertToVO(payment);
    }

    @Override
    public PaymentVO getPaymentById(Long paymentId) {
        Payment payment = this.getById(paymentId);
        if (payment == null || payment.getIsDelete() == 1) {
            return null;
        }
        return convertToVO(payment);
    }

    @Override
    public PaymentVO generateVoucher(Long paymentId) {
        Payment payment = this.getById(paymentId);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "支付记录不存在");
        }

        if (!"paid".equals(payment.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED_ERROR, "只有已完成的支付才能生成凭证");
        }

        // 如果还没有凭证号，生成一个
        if (payment.getVoucherNo() == null || payment.getVoucherNo().isEmpty()) {
            payment.setVoucherNo("VCH" + payment.getPaymentNo().substring(3));
            payment.setUpdateTime(LocalDateTime.now());
            this.updateById(payment);
        }

        return convertToVO(payment);
    }

    /**
     * 将Payment实体转换为PaymentVO
     *
     * @param payment Payment实体
     * @return PaymentVO
     */
    private PaymentVO convertToVO(Payment payment) {
        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(payment, vo);
        // 设置状态文本
        vo.setPaymentStatusText(getStatusText(payment.getPaymentStatus()));
        return vo;
    }

    /**
     * 获取支付状态文本
     *
     * @param status 状态码
     * @return 状态文本
     */
    private String getStatusText(String status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case "unpaid":
                return "未支付";
            case "processing":
                return "处理中";
            case "paid":
                return "已支付";
            case "failed":
                return "支付失败";
            default:
                return "未知";
        }
    }
}
