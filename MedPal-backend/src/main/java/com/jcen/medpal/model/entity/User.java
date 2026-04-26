package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@TableName(value = "user")
@Data
public class User implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 开放平台id
     */
    private String unionId;

    /**
     * 公众号openId
     */
    private String mpOpenId;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 账户状态：1启用 0禁用
     */
    private Integer status;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 身份证号（陪诊员）
     */
    private String idCard;

    /**
     * 身份证正面（陪诊员）
     */
    private String idCardFront;

    /**
     * 身份证反面（陪诊员）
     */
    private String idCardBack;

    /**
     * 资质证书（陪诊员）
     */
    private String qualificationCert;

    /**
     * 资质类型（陪诊员）
     */
    private String qualificationType;

    /**
     * 工作年限（陪诊员）
     */
    private Integer workYears;

    /**
     * 专长（陪诊员）
     */
    private String specialties;

    /**
     * 服务区域（陪诊员）
     */
    private String serviceArea;

    /**
     * 实名认证状态（陪诊员）
     */
    private String realNameStatus;

    /**
     * 资质认证状态（陪诊员）
     */
    private String qualificationStatus;

    /**
     * 评分（陪诊员）
     */
    private Double rating;

    /**
     * 服务次数（陪诊员）
     */
    private Integer serviceCount;

    /**
     * 总收入（陪诊员）
     */
    private Double totalIncome;

    /**
     * 年龄（患者）
     */
    private Integer age;

    /**
     * 性别（患者）
     */
    private String gender;

    /**
     * 病史（患者）
     */
    private String medicalHistory;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}