package com.jcen.medpal.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新个人信息请求
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 性别
     */
    private String gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 病史
     */
    private String medicalHistory;

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

    private static final long serialVersionUID = 1L;
}
