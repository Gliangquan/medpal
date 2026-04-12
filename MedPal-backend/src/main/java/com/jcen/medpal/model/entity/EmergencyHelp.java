package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("emergency_help")
public class EmergencyHelp implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String content;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private Long responderId;
    private String resolveNote;
    private LocalDateTime resolveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
