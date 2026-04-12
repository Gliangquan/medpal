package com.jcen.medpal.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录VO
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
public class PaymentVO implements Serializable {

    /**
     * 支付ID
     */
    private Long id;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付渠道
     */
    private String paymentChannel;

    /**
     * 支付状态
     */
    private String paymentStatus;

    /**
     * 支付状态描述
     */
    private String paymentStatusText;

    /**
     * 第三方支付流水号
     */
    private String transactionId;

    /**
     * 支付时间
     */
    private LocalDateTime paidTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 支付凭证号
     */
    private String voucherNo;

    /**
     * 支付描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}
