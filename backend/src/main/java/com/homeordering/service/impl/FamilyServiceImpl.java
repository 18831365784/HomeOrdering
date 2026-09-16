package com.homeordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.dto.response.FamilyDTO;
import com.homeordering.dto.response.FamilyMemberDTO;
import com.homeordering.entity.Family;
import com.homeordering.entity.User;
import com.homeordering.mapper.FamilyMapper;
import com.homeordering.mapper.UserMapper;
import com.homeordering.service.FamilyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 家庭服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyMapper familyMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public FamilyDTO createFamily(String uuid, String name) {
        String familyName = normalizeFamilyName(name);
        log.info("创建家庭: uuid={}, name={}", uuid, familyName);
        // 防止旧前端把 uuid 当成 name 传入
        if (familyName.equals(uuid)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "家庭名称无效，请重新输入");
        }

        User user = getUserByUuid(uuid);
        if (user.getFamilyId() != null) {
            throw new BusinessException(ErrorCode.FAMILY_ALREADY_JOINED);
        }

        Family family = new Family();
        family.setName(familyName);
        family.setInviteCode(generateInviteCode());
        family.setAdminUuid(uuid);
        familyMapper.insert(family);

        // 再查一遍，避免返回对象字段异常
        family = familyMapper.selectById(family.getId());

        user.setFamilyId(family.getId());
        user.setRole(1);
        userMapper.updateById(user);

        return convertToDTO(family, uuid);
    }

    @Override
    @Transactional
    public FamilyDTO joinFamily(String uuid, String inviteCode) {
        log.info("加入家庭: uuid={}, inviteCode={}", uuid, inviteCode);
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new BusinessException(ErrorCode.FAMILY_INVITE_CODE_INVALID);
        }

        // 检查用户是否已有家庭
        User user = getUserByUuid(uuid);
        if (user.getFamilyId() != null) {
            throw new BusinessException(ErrorCode.FAMILY_ALREADY_JOINED);
        }

        // 通过邀请码查找家庭
        Family family = familyMapper.selectOne(
                new LambdaQueryWrapper<Family>()
                        .eq(Family::getInviteCode, inviteCode)
        );

        if (family == null) {
            throw new BusinessException(ErrorCode.FAMILY_INVITE_CODE_INVALID);
        }

        // 更新用户的家庭ID（成员 role 保持 0，权限以 admin_uuid 为准）
        user.setFamilyId(family.getId());
        user.setRole(0);
        userMapper.updateById(user);

        return convertToDTO(family, uuid);
    }

    @Override
    public FamilyDTO getFamilyInfo(String uuid) {
        User user = getUserByUuid(uuid);
        if (user.getFamilyId() == null) {
            return null; // 用户没有家庭
        }

        Family family = familyMapper.selectById(user.getFamilyId());
        if (family == null) {
            return null;
        }

        return convertToDTO(family, uuid);
    }

    @Override
    public List<FamilyMemberDTO> getFamilyMembers(String uuid) {
        User user = getUserByUuid(uuid);
        if (user.getFamilyId() == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }

        List<User> members = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getFamilyId, user.getFamilyId())
        );

        Family family = familyMapper.selectById(user.getFamilyId());

        List<FamilyMemberDTO> result = new ArrayList<>();
        for (User member : members) {
            FamilyMemberDTO dto = new FamilyMemberDTO();
            dto.setUuid(member.getUuid());
            dto.setNickname(member.getNickname());
            dto.setAvatarUrl(member.getAvatarUrl());
            dto.setIsAdmin(family.getAdminUuid().equals(member.getUuid()));
            dto.setBalance(member.getBalance());
            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional
    public String regenerateInviteCode(String uuid) {
        User user = getUserByUuid(uuid);
        if (user.getFamilyId() == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }

        Family family = familyMapper.selectById(user.getFamilyId());
        if (!family.getAdminUuid().equals(uuid)) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }

        family.setInviteCode(generateInviteCode());
        familyMapper.updateById(family);

        return family.getInviteCode();
    }

    @Override
    @Transactional
    public FamilyDTO updateFamilyName(String uuid, String name) {
        String familyName = normalizeFamilyName(name);
        User user = getUserByUuid(uuid);
        if (user.getFamilyId() == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }
        Family family = familyMapper.selectById(user.getFamilyId());
        if (family == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }
        if (!uuid.equals(family.getAdminUuid())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }
        if (familyName.equals(uuid)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "家庭名称无效，请重新输入");
        }
        family.setName(familyName);
        familyMapper.updateById(family);
        return convertToDTO(familyMapper.selectById(family.getId()), uuid);
    }

    @Override
    public boolean isFamilyAdmin(String uuid) {
        User user = getUserByUuid(uuid);
        if (user.getFamilyId() == null) {
            return false;
        }

        Family family = familyMapper.selectById(user.getFamilyId());
        return family != null && family.getAdminUuid().equals(uuid);
    }

    private User getUserByUuid(String uuid) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUuid, uuid)
        );
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private FamilyDTO convertToDTO(Family family, String currentUuid) {
        FamilyDTO dto = new FamilyDTO();
        dto.setId(family.getId());
        dto.setName(family.getName());
        dto.setInviteCode(family.getInviteCode());
        dto.setIsAdmin(family.getAdminUuid().equals(currentUuid));

        // 获取成员列表
        List<User> members = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getFamilyId, family.getId())
        );

        List<FamilyMemberDTO> memberDTOs = new ArrayList<>();
        for (User member : members) {
            FamilyMemberDTO memberDTO = new FamilyMemberDTO();
            memberDTO.setUuid(member.getUuid());
            memberDTO.setNickname(member.getNickname());
            memberDTO.setAvatarUrl(member.getAvatarUrl());
            memberDTO.setIsAdmin(family.getAdminUuid().equals(member.getUuid()));
            memberDTO.setBalance(member.getBalance());
            memberDTOs.add(memberDTO);
        }
        dto.setMembers(memberDTOs);

        return dto;
    }

    private String normalizeFamilyName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "请输入家庭名称");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 20) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "家庭名称不能超过20个字");
        }
        return trimmed;
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}
