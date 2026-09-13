package com.homeordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeordering.dto.WxLoginDTO;
import com.homeordering.entity.User;
import com.homeordering.mapper.UserMapper;
import com.homeordering.service.UserService;
import com.homeordering.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 用户Service实现类
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Value("${wx.appid}")
    private String appid;

    @Value("${wx.secret}")
    private String secret;

    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    @Override
    public UserVO wxLogin(WxLoginDTO wxLoginDTO) {
        String openid = getOpenidFromWx(wxLoginDTO.getCode());
        if (openid == null) {
            throw new RuntimeException("获取openid失败");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid)
        );

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUuid(java.util.UUID.randomUUID().toString());
            user.setNickname(wxLoginDTO.getNickname());
            user.setAvatarUrl(wxLoginDTO.getAvatarUrl());
            user.setRole(0);
            userMapper.insert(user);
            log.info("创建新用户: openid={}, uuid={}", openid, user.getUuid());
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setToken(generateToken(user));

        return userVO;
    }

    @Override
    public boolean isAdmin(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUuid, uuid)
        );
        return user != null && user.getRole() == 1;
    }

    private String getOpenidFromWx(String code) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format(WX_LOGIN_URL, appid, secret, code);

            String responseStr = restTemplate.getForObject(url, String.class);
            log.info("微信接口返回: {}", responseStr);

            if (responseStr != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(responseStr, Map.class);

                if (result.containsKey("openid")) {
                    return (String) result.get("openid");
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
}