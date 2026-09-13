package com.homeordering.service;

import com.homeordering.dto.response.FamilyDTO;
import com.homeordering.dto.response.FamilyMemberDTO;

import java.util.List;

/**
 * 家庭服务接口
 */
public interface FamilyService {

    /**
     * 创建家庭
     */
    FamilyDTO createFamily(String uuid, String name);

    /**
     * 通过邀请码加入家庭
     */
    FamilyDTO joinFamily(String uuid, String inviteCode);

    /**
     * 获取用户家庭信息
     */
    FamilyDTO getFamilyInfo(String uuid);

    /**
     * 获取家庭成员列表
     */
    List<FamilyMemberDTO> getFamilyMembers(String uuid);

    /**
     * 重新生成邀请码（管理员）
     */
    String regenerateInviteCode(String uuid);

    /**
     * 判断用户是否是家庭管理员
     */
    boolean isFamilyAdmin(String uuid);
}
