package com.jcen.medpal.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 陪诊员认证审核请求
 */
@Data
public class UserCertificationAuditRequest implements Serializable {

    /**
     * 陪诊员用户ID
     */
    private Long userId;

    /**
     * 实名认证状态：approved / rejected
     */
    private String realNameStatus;

    /**
     * 资质认证状态：approved / rejected
     */
    private String qualificationStatus;

    private static final long serialVersionUID = 1L;
}
