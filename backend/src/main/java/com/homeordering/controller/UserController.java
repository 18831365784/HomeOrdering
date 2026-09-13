package com.homeordering.controller;

import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.common.Result;
import com.homeordering.dto.request.UpdateUserDTO;
import com.homeordering.dto.response.UserDTO;
import com.homeordering.entity.User;
import com.homeordering.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    @GetMapping("/info")
    public Result<UserDTO> getUserInfo(@RequestParam String uuid) {
        log.info("获取用户信息: uuid={}", uuid);

        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUuid, uuid)
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return Result.success(convertToDTO(user));
    }

    @PutMapping("/update")
    public Result<UserDTO> updateUser(@RequestBody UpdateUserDTO updateDTO) {
        log.info("更新用户信息: uuid={}", updateDTO.getUuid());

        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUuid, updateDTO.getUuid())
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDTO.getAvatarUrl());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getAddress() != null) {
            user.setAddress(updateDTO.getAddress());
        }

        userMapper.updateById(user);
        return Result.success("更新成功", convertToDTO(user));
    }

    @GetMapping("/checkAdmin")
    public Result<Boolean> checkAdmin(@RequestParam String uuid) {
        log.info("检查管理员: uuid={}", uuid);

        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUuid, uuid)
        );

        boolean isAdmin = user != null && user.getRole() != null && user.getRole() == 1;
        return Result.success(isAdmin);
    }

    @PutMapping("/balance")
    public Result<UserDTO> updateBalance(@RequestBody java.util.Map<String, Object> params) {
        String uuid = (String) params.get("uuid");
        Object balanceObj = params.get("balance");
        BigDecimal balance = new BigDecimal(balanceObj.toString());
        log.info("更新余额: uuid={}, balance={}", uuid, balance);

        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUuid, uuid)
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        user.setBalance(balance);
        userMapper.updateById(user);

        return Result.success("余额更新成功", convertToDTO(user));
    }

    @PutMapping("/profile")
    public Result<UserDTO> updateProfile(@RequestBody UpdateUserDTO updateDTO) {
        log.info("更新个人资料: uuid={}", updateDTO.getUuid());

        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUuid, updateDTO.getUuid())
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDTO.getAvatarUrl());
        }

        userMapper.updateById(user);
        return Result.success("更新成功", convertToDTO(user));
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setIsAdmin(user.getRole() != null && user.getRole() == 1);
        return dto;
    }
}