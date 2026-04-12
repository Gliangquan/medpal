package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("hospital")
public class Hospital implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String hospitalName;
    private String hospitalLevel;
    private String address;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
    private String phone;
    private String introduction;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
