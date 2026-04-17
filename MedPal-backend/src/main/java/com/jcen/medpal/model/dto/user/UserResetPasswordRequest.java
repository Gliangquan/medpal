package com.jcen.medpal.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户重置密码请求
 */
@Data
public class UserResetPasswordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
