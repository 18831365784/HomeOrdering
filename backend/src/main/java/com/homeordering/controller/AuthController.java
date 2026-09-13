package com.homeordering.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeordering.common.Result;
import com.homeordering.dto.request.LoginDTO;
import com.homeordering.dto.response.AuthDTO;
import com.homeordering.dto.response.UserDTO;
import com.homeordering.entity.User;
import com.homeordering.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;

    @Value("${wx.appid}")
    private String appid;

    @Value("${wx.secret}")
    private String secret;

    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    @PostMapping("/login")
    public Result<AuthDTO> wxLogin(@RequestBody LoginDTO loginDTO) {
        log.info("微信登录请求: nickname={}", loginDTO.getNickname());

        String openid = getOpenidFromWx(loginDTO.getCode());
        if (openid == null) {
            return Result.error("获取openid失败");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid)
        );

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUuid(UUID.randomUUID().toString());
            user.setNickname(loginDTO.getNickname());
            user.setAvatarUrl(loginDTO.getAvatarUrl());
            user.setRole(0);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
            log.info("创建新用户: openid={}", openid);
        } else {
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }

        String token = generateToken(user);
        return Result.success("登录成功", new AuthDTO(convertToDTO(user), token));
    }

    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return Result.error("未登录");
        }

        String uuid = extractUuidFromToken(token);
        if (uuid == null) {
            return Result.error("无效的登录凭证");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUuid, uuid)
        );

        if (user == null) {
            return Result.error("用户不存在");
        }

        return Result.success(convertToDTO(user));
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.success("退出成功");
    }

    private String getOpenidFromWx(String code) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format(WX_LOGIN_URL, appid, secret, code);
            log.info("调用微信登录接口: {}", url);

            String responseStr = restTemplate.getForObject(url, String.class);
            log.info("微信接口返回: {}", responseStr);

            if (responseStr != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(responseStr, Map.class);

                if (result.containsKey("openid")) {
                    return (String) result.get("openid");
                }

                // errcode 是秒验返回，不是错误
                if (result.containsKey("session_key")) {
                    // 第三方服务器登录也不返回openid，这种情况直接用unionid或新建
                    log.warn("微信返回了session_key但没有openid");
                }
                
                log.error("微信登录失败: {}", result);
            }

            return null;
        } catch (Exception e) {
            log.error("调用微信接口失败", e);
            return null;
        }
    }

    private String generateToken(User user) {
        return user.getUuid() + "_" + System.currentTimeMillis();
    }

    private String extractUuidFromToken(String token) {
        if (token == null || !token.contains("_")) {
            return null;
        }
        return token.split("_")[0];
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setIsAdmin(user.getRole() != null && user.getRole() == 1);
        return dto;
    }
}