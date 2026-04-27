package com.jcen.medpal.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 已登录用户视图（脱敏）
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 **/
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

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
     * 手机号
     */
    private String userPhone;

    /**
     * 邮箱
     */
    private String userEmail;

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

    /**
     * 实名认证状态（陪诊员）
     */
    private String realNameStatus;

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
     * 资质认证状态（陪诊员）
     */
    private String qualificationStatus;

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
     * JWT Token
     */
    private String token;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否是新用户（微信登录时使用）
     */
    private Boolean isNewUser;

    private static final long serialVersionUID = 1L;
}
