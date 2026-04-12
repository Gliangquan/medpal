package com.jcen.medpal.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class WeChatLoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;
    private String nickName;
    private String avatarUrl;
    private String userInfo;
}
