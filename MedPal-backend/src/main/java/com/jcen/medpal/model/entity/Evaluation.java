package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("evaluation")
public class Evaluation implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    private Long userId;
    private Long companionId;
    private Integer professionalismScore;
    private Integer attitudeScore;
    private Integer efficiencyScore;
    private Integer satisfactionScore;
    private java.math.BigDecimal averageScore;
    private String evaluationText;
    private String evaluationImages;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
