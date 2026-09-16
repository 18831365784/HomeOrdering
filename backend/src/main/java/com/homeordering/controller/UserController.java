package com.homeordering.controller;

import com.homeordering.common.AuthContext;
import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.common.Result;
import com.homeordering.dto.request.UpdateUserDTO;
import com.homeordering.dto.response.UserDTO;
import com.homeordering.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public Result<UserDTO> getUserInfo(@RequestParam(required = false) String uuid) {
        String target = uuid != null && !uuid.isBlank() ? uuid : AuthContext.requireUuid();
        return Result.success(userService.getInfo(target));
    }

    @PutMapping("/profile")
    public Result<UserDTO> updateProfile(@RequestBody UpdateUserDTO updateDTO) {
        return Result.success("更新成功", userService.updateUser(updateDTO));
    }

    @PutMapping("/balance")
    public Result<UserDTO> updateBalance(@RequestBody Map<String, Object> params) {
        Object balanceObj = params.get("balance");
        if (balanceObj == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID);
        }
        String targetUuid = params.get("uuid") != null ? params.get("uuid").toString() : AuthContext.requireUuid();
        BigDecimal balance = new BigDecimal(balanceObj.toString());
        return Result.success("余额更新成功",
                userService.updateBalance(AuthContext.requireUuid(), targetUuid, balance));
    }
}
