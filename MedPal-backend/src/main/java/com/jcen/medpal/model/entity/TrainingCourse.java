package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("training_course")
public class TrainingCourse implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    private String category;
    private String description;
    private String content;
    private Integer difficulty;
    private Integer duration;
    private String instructor;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
