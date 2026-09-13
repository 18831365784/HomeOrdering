package com.homeordering.dto.request;

import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginDTO {

    private String code;

    private String nickname;

    private String avatarUrl;
}