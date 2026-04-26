package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生实体类
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
@TableName("doctor")
public class Doctor implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 医院ID
     */
    private Long hospitalId;
    
    /**
     * 科室ID
     */
    private Long departmentId;
    
    /**
     * 医生名称
     */
    private String doctorName;

    /**
     * 医生职称
     */
    private String doctorTitle;

    /**
     * 专业特长
     */
    private String specialties;

    /**
     * 医生简介
     */
    private String introduction;

    /**
     * 出诊时间
     */
    private String clinicTime;
    
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
    @TableLogic
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
