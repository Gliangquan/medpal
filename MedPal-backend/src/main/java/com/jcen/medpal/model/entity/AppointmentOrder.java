package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("appointment_order")
public class AppointmentOrder implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    private Long userId;
    private Long companionId;
    private Long hospitalId;
    private Long departmentId;
    private Long doctorId;
    private LocalDateTime appointmentDate;
    private String duration;
    private String specificNeeds;
    private String orderStatus;
    private java.math.BigDecimal totalPrice;
    private java.math.BigDecimal serviceFee;
    private java.math.BigDecimal extraFee;
    private java.math.BigDecimal platformFee;
    private String paymentStatus;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private LocalDateTime completionRequestedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
