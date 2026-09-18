package com.homeordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeordering.common.AuthContext;
import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.dto.request.LoginDTO;
import com.homeordering.dto.request.UpdateUserDTO;
import com.homeordering.dto.response.AuthDTO;
import com.homeordering.dto.response.UserDTO;
import com.homeordering.entity.User;
import com.homeordering.mapper.UserMapper;
import com.homeordering.service.FamilyAccessService;
import com.homeordering.service.UserService;
import com.homeordering.util.FileUrlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final FamilyAccessService familyAccessService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FileUrlHelper fileUrlHelper;

    @Value("${wx.appid}")
    private String appid;

    @Value("${wx.secret}")
    private String secret;

    private static final String WX_LOGIN_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    @Override
    public AuthDTO login(LoginDTO loginDTO) {
        if (appid == null || appid.isBlank() || secret == null || secret.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "未配置微信 AppId/Secret");
        }
        String openid = getOpenidFromWx(loginDTO.getCode());
        if (openid == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "获取openid失败");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        LocalDateTime now = LocalDateTime.now();
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUuid(UUID.randomUUID().toString());
            user.setNickname(loginDTO.getNickname());
            user.setAvatarUrl(fileUrlHelper.toStoredPath(loginDTO.getAvatarUrl()));
            user.setRole(0);
            user.setBalance(BigDecimal.ZERO);
            user.setCreateTime(now);
            user.setUpdateTime(now);
            user.setLastLoginTime(now);
            userMapper.insert(user);
        } else {
            if (loginDTO.getNickname() != null) {
                user.setNickname(loginDTO.getNickname());
            }
            if (loginDTO.getAvatarUrl() != null) {
                user.setAvatarUrl(fileUrlHelper.toStoredPath(loginDTO.getAvatarUrl()));
            }
            user.setLastLoginTime(now);
            user.setUpdateTime(now);
            userMapper.updateById(user);
        }
        String token = user.getUuid() + "_" + System.currentTimeMillis();
        return new AuthDTO(toDto(user), token);
    }

    @Override
    public UserDTO getCurrentUser() {
        return toDto(AuthContext.requireUser());
    }

    @Override
    public UserDTO getInfo(String uuid) {
        User actor = AuthContext.requireUser();
        User target = familyAccessService.requireUser(uuid);
        if (!actor.getUuid().equals(target.getUuid())) {
            if (actor.getFamilyId() == null || target.getFamilyId() == null
                    || !actor.getFamilyId().equals(target.getFamilyId())) {
                throw new BusinessException(ErrorCode.FAMILY_NOT_MEMBER);
            }
        }
        return toDto(target);
    }

    @Override
    public UserDTO updateUser(UpdateUserDTO updateDTO) {
        User actor = AuthContext.requireUser();
        User user = actor;
        if (updateDTO.getUuid() != null && !updateDTO.getUuid().isBlank()
                && !updateDTO.getUuid().equals(actor.getUuid())) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(fileUrlHelper.toStoredPath(updateDTO.getAvatarUrl()));
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getAddress() != null) {
            user.setAddress(updateDTO.getAddress());
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return toDto(user);
    }

    @Override
    public UserDTO updateBalance(String operatorUuid, String targetUuid, BigDecimal balance) {
        familyAccessService.requireAdmin(operatorUuid);
        User target = familyAccessService.requireUser(targetUuid);
        User operator = familyAccessService.requireUser(operatorUuid);
        if (operator.getFamilyId() == null || !operator.getFamilyId().equals(target.getFamilyId())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_MEMBER);
        }
        target.setBalance(balance);
        target.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(target);
        return toDto(target);
    }

    @Override
    public UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setAvatarUrl(fileUrlHelper.toPublicUrl(user.getAvatarUrl()));
        dto.setIsAdmin(familyAccessService.isFamilyAdmin(user.getUuid()));
        dto.setRole(Boolean.TRUE.equals(dto.getIsAdmin()) ? 1 : 0);
        return dto;
    }

    private String getOpenidFromWx(String code) {
        try {
            String url = String.format(WX_LOGIN_URL, appid, secret, code);
            String responseStr = restTemplate.getForObject(url, String.class);
            log.info("微信登录返回: {}", responseStr);
            if (responseStr == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(responseStr, Map.class);
            if (result.containsKey("openid")) {
                return (String) result.get("openid");
            }
            log.error("微信登录失败: {}", result);
            return null;
        } catch (Exception e) {
            log.error("调用微信接口失败", e);
            return null;
        }
    }
}
