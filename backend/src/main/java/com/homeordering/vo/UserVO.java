package com.homeordering.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户信息VO
 */
@Data
public class UserVO implements Serializable {
    
    private Long id;
    private String openid;
    private String uuid;
    private String nickname;
    private String avatarUrl;
    private Integer role;
    private String token;
}
