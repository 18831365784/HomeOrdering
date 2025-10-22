package com.homeordering.controller;

import com.homeordering.common.Result;
import com.homeordering.dto.WxLoginDTO;
import com.homeordering.service.UserService;
import com.homeordering.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户Controller
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 微信登录
     */
    @PostMapping("/wxLogin")
    public Result<UserVO> wxLogin(@RequestBody WxLoginDTO wxLoginDTO) {
        log.info("微信登录: {}", wxLoginDTO);
        
        try {
            UserVO userVO = userService.wxLogin(wxLoginDTO);
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("微信登录失败", e);
            return Result.error("登录失败");
        }
    }
    
    /**
     * 检查是否为管理员
     */
    @GetMapping("/checkAdmin")
    public Result<Boolean> checkAdmin(@RequestParam String uuid) {
        boolean isAdmin = userService.isAdmin(uuid);
        return Result.success(isAdmin);
    }
}
