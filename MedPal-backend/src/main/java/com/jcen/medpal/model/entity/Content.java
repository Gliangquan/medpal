package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("content")
public class Content implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    private String type;
    private String content;
    private String summary;
    private String coverImage;
    private String tags;
    private Integer priority;
    private String status;
    private Integer viewCount;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
