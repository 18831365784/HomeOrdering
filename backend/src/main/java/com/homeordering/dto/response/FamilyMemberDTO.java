package com.homeordering.dto.response;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 家庭成员响应DTO
 */
@Data
public class FamilyMemberDTO {

    private String uuid;

    private String nickname;

    private String avatarUrl;

    private Boolean isAdmin;

    private BigDecimal balance;
}
