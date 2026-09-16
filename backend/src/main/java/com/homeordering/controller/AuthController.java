package com.homeordering.controller;

import com.homeordering.common.Result;
import com.homeordering.dto.request.LoginDTO;
import com.homeordering.dto.response.AuthDTO;
import com.homeordering.dto.response.UserDTO;
import com.homeordering.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<AuthDTO> login(@RequestBody LoginDTO loginDTO) {
        return Result.success("登录成功", userService.login(loginDTO));
    }

    @GetMapping("/me")
    public Result<UserDTO> me() {
        return Result.success(userService.getCurrentUser());
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        // 无服务端会话，客户端丢弃 token 即可
        return Result.success("退出成功");
    }
}
