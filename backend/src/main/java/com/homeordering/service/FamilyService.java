package com.homeordering.service;

import com.homeordering.dto.response.FamilyDTO;
import com.homeordering.dto.response.FamilyMemberDTO;

import java.util.List;

public interface FamilyService {

    FamilyDTO createFamily(String uuid, String name);

    FamilyDTO joinFamily(String uuid, String inviteCode);

    FamilyDTO getFamilyInfo(String uuid);

    List<FamilyMemberDTO> getFamilyMembers(String uuid);

    String regenerateInviteCode(String uuid);

    FamilyDTO updateFamilyName(String uuid, String name);

    boolean isFamilyAdmin(String uuid);
}
