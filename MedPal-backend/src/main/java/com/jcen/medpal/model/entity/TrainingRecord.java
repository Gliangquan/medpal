package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("training_record")
public class TrainingRecord implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long companionId;
    private Long courseId;
    private String status;
    private Integer progress;
    private Integer learningTime;
    private LocalDateTime startTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
