package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("service_record")
public class ServiceRecord implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    private Long companionId;
    private String visitProcess;
    private String doctorAdvice;
    private String conditionChanges;
    private String serviceSummary;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
