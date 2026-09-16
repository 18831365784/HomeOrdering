package com.homeordering.service;

import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.entity.Family;
import com.homeordering.entity.User;
import com.homeordering.mapper.FamilyMapper;
import com.homeordering.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FamilyAccessService {

    private final UserMapper userMapper;
    private final FamilyMapper familyMapper;

    public User requireUser(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUuid, uuid));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public Family requireFamily(User user) {
        if (user.getFamilyId() == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }
        Family family = familyMapper.selectById(user.getFamilyId());
        if (family == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }
        return family;
    }

    public Family requireAdmin(String uuid) {
        User user = requireUser(uuid);
        Family family = requireFamily(user);
        if (!uuid.equals(family.getAdminUuid())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }
        return family;
    }

    public boolean isFamilyAdmin(String uuid) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUuid, uuid));
        if (user == null || user.getFamilyId() == null) {
            return false;
        }
        Family family = familyMapper.selectById(user.getFamilyId());
        return family != null && uuid.equals(family.getAdminUuid());
    }

    public User requireSameFamily(Long familyId, String uuid) {
        User user = requireUser(uuid);
        if (familyId == null || user.getFamilyId() == null || !familyId.equals(user.getFamilyId())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_MEMBER);
        }
        return user;
    }
}
