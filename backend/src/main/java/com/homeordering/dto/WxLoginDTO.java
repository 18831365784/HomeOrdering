package com.homeordering.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 微信登录请求DTO
 */
@Data
public class WxLoginDTO implements Serializable {
    
    /**
     * 微信登录code
     */
    private String code;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatarUrl;
}
