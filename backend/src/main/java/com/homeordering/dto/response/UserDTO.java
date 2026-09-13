package com.homeordering.dto.response;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 用户响应DTO
 */
@Data
public class UserDTO {

    private Long id;

    private String uuid;

    private String nickname;

    private String avatarUrl;

    private String phone;

    private String address;

    private Integer role;

    private Boolean isAdmin;

    private BigDecimal balance;

    /**
     * 所属家庭ID
     */
    private Long familyId;
}