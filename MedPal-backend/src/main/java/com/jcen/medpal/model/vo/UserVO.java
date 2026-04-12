package com.jcen.medpal.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户视图（脱敏）
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private Long id;

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
     * 用户账号
     */
    private String userAccount;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 评分（陪诊员）
     */
    private Double rating;

    /**
     * 服务次数（陪诊员）
     */
    private Integer serviceCount;

    /**
     * 工作年限（陪诊员）
     */
    private Integer workYears;

    /**
     * 服务区域（陪诊员）
     */
    private String serviceArea;

    /**
     * 专长（陪诊员）
     */
    private String specialties;

    /**
     * 总收入（陪诊员）
     */
    private Double totalIncome;

    /**
     * 实名认证状态（陪诊员）
     */
    private String realNameStatus;

    /**
     * 资质认证状态（陪诊员）
     */
    private String qualificationStatus;

    /**
     * 资质类型（陪诊员）
     */
    private String qualificationType;

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
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
