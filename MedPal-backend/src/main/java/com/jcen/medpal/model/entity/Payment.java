package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
@TableName("payment")
public class Payment implements Serializable {

    /**
     * 支付ID
     */
    @TableId(type = IdType.AUTO)
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
     * 支付渠道（mock/alipay/wechat等）
     */
    private String paymentChannel;

    /**
     * 支付状态：unpaid-未支付，processing-处理中，paid-已支付，failed-支付失败
     */
    private String paymentStatus;

    /**
     * 第三方支付流水号（mock支付时可为空）
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

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
