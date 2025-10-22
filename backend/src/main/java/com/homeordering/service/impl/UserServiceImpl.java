package com.homeordering.service.impl;

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
import java.util.UUID;

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
        // 1. 调用微信接口获取openid
        String openid = getOpenidFromWx(wxLoginDTO.getCode());
        
        // 开发测试模式：如果微信接口调用失败，使用code作为openid
        if (openid == null) {
            log.warn("微信接口调用失败，使用开发测试模式");
            openid = "test_" + wxLoginDTO.getCode();
        }
        
        // 2. 查询用户是否存在
        User user = userMapper.selectByOpenid(openid);
        
        if (user == null) {
            // 3. 新用户，创建用户记录
            user = new User();
            user.setOpenid(openid);
            user.setUuid(UUID.randomUUID().toString());
            user.setNickname(wxLoginDTO.getNickname());
            user.setAvatarUrl(wxLoginDTO.getAvatarUrl());
            user.setRole(0); // 默认普通用户
            userMapper.insert(user);
            log.info("创建新用户: openid={}, uuid={}", openid, user.getUuid());
        } else {
            // 4. 老用户，更新用户信息
            user.setNickname(wxLoginDTO.getNickname());
            user.setAvatarUrl(wxLoginDTO.getAvatarUrl());
            userMapper.update(user);
            log.info("更新用户信息: openid={}, uuid={}", openid, user.getUuid());
        }
        
        // 5. 构造返回对象
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setToken(generateToken(user)); // 生成token
        
        return userVO;
    }
    
    @Override
    public boolean isAdmin(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }
        
        // 直接检查数据库中的角色
        User user = userMapper.selectByUuid(uuid);
        return user != null && user.getRole() == 1;
    }
    
    /**
     * 调用微信接口获取openid
     */
    private String getOpenidFromWx(String code) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format(WX_LOGIN_URL, appid, secret, code);
            
            // 使用String接收响应，然后手动解析JSON
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
    
    /**
     * 生成token（简化版，实际应使用JWT）
     */
    private String generateToken(User user) {
        return user.getUuid() + "_" + System.currentTimeMillis();
    }
}
