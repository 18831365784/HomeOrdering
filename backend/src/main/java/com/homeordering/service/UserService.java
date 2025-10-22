package com.homeordering.service;

import com.homeordering.dto.WxLoginDTO;
import com.homeordering.vo.UserVO;

/**
 * 用户Service接口
 */
public interface UserService {
    
    /**
     * 微信登录
     */
    UserVO wxLogin(WxLoginDTO wxLoginDTO);
    
    /**
     * 根据UUID检查是否为管理员
     */
    boolean isAdmin(String uuid);
}
