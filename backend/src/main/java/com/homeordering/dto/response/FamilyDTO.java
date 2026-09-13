package com.homeordering.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 家庭信息响应DTO
 */
@Data
public class FamilyDTO {

    private Long id;

    private String name;

    private String inviteCode;

    private Boolean isAdmin;

    private List<FamilyMemberDTO> members;
}
