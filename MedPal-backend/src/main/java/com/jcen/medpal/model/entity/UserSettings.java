package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_settings")
public class UserSettings implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    // 通知设置
    private Integer orderNotificationEnabled;
    private Integer activityNotificationEnabled;
    private Integer healthPushEnabled;
    private Integer chatNotificationEnabled;
    
    // 隐私设置
    private Integer medicalRecordVisible;
    private Integer profileVisible;
    private Integer chatHistorySaveDays;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
