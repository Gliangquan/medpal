package com.jcen.medpal.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单详情 VO（包含关联信息）
 */
@Data
public class OrderDetailVO {
    
    private Long id;
    private String orderNo;
    private Long userId;
    private Long companionId;
    private Long hospitalId;
    private Long departmentId;
    private Long doctorId;
    
    // 关联信息
    private String hospitalName;
    private String departmentName;
    private String doctorName;
    private String companionName;
    private String companionPhone;
    private String patientPhone;
    
    private LocalDateTime appointmentDate;
    private String duration;
    private String specificNeeds;
    private String orderStatus;
    private String paymentStatus;
    private BigDecimal totalPrice;
    private BigDecimal serviceFee;
    private BigDecimal extraFee;
    private BigDecimal platformFee;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private LocalDateTime completionRequestedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
