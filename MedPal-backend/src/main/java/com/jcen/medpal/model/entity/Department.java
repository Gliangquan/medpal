package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 科室实体类
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
@TableName("department")
public class Department implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 医院ID
     */
    private Long hospitalId;
    
    /**
     * 科室名称
     */
    private String departmentName;

    /**
     * 科室代码
     */
    private String departmentCode;

    /**
     * 科室介绍
     */
    private String introduction;
    
    /**
     * 状态（1-正常，0-禁用）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否删除（0-未删除，1-已删除）
     */
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
