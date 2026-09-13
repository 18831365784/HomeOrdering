package com.homeordering.dto.request;

import lombok.Data;

/**
 * 用户信息更新请求DTO
 */
@Data
public class UpdateUserDTO {

    private String uuid;

    private String nickname;

    private String avatarUrl;

    private String phone;

    private String address;
}