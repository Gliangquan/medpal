package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("medical_record")
public class MedicalRecord implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String recordNo;
    private String hospitalName;
    private String departmentName;
    private String doctorName;
    private LocalDateTime visitDate;
    private String diagnosis;
    private String symptoms;
    private String treatment;
    private String prescription;
    private String checkResults;
    private String doctorAdvice;
    private String attachments;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
