package com.homeordering.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应DTO
 */
@Data
@NoArgsConstructor
public class AuthDTO {

    private UserDTO user;

    private String token;

    /** openid 在库中不存在，本次新建 */
    private Boolean newUser;

    /** 已有真实昵称，不必再走完善资料 */
    private Boolean profileCompleted;
}
