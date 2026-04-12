package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("settlement")
public class Settlement implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long companionId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime settlementTime;
    private String settlementNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    private static final long serialVersionUID = 1L;
}
