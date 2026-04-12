package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    
    private Long senderId;
    
    private String senderType;
    
    private Long receiverId;
    
    private String messageType;
    
    private String content;
    
    private String imageUrl;
    
    private Integer isRead;
    
    private LocalDateTime createTime;
    
    private Integer isDelete;
    
    // 撤回相关字段
    @TableField(exist = false)
    private Integer isRecalled;
    
    @TableField(exist = false)
    private LocalDateTime recalledTime;
    
    @TableField(exist = false)
    private String recallReason;
    
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
