package com.homeordering.controller;

import com.homeordering.common.AuthContext;
import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.common.Result;
import com.homeordering.dto.request.CreateFamilyRequest;
import com.homeordering.dto.request.UpdateFamilyRequest;
import com.homeordering.dto.response.FamilyDTO;
import com.homeordering.dto.response.FamilyMemberDTO;
import com.homeordering.service.FamilyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping
    public Result<FamilyDTO> createFamily(@RequestBody CreateFamilyRequest request) {
        if (request == null || request.getName() == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "请输入家庭名称");
        }
        FamilyDTO family = familyService.createFamily(AuthContext.requireUuid(), request.getName());
        return Result.success("创建成功", family);
    }

    @PostMapping("/join")
    public Result<FamilyDTO> joinFamily(@RequestBody Map<String, String> params) {
        String inviteCode = params.get("inviteCode");
        FamilyDTO family = familyService.joinFamily(AuthContext.requireUuid(), inviteCode);
        return Result.success("加入成功", family);
    }

    @GetMapping("/info")
    public Result<FamilyDTO> getFamilyInfo() {
        return Result.success(familyService.getFamilyInfo(AuthContext.requireUuid()));
    }

    @GetMapping("/members")
    public Result<List<FamilyMemberDTO>> getFamilyMembers() {
        return Result.success(familyService.getFamilyMembers(AuthContext.requireUuid()));
    }

    @PostMapping("/regenerate-code")
    public Result<String> regenerateInviteCode() {
        String newCode = familyService.regenerateInviteCode(AuthContext.requireUuid());
        return Result.success("生成成功", newCode);
    }

    @PutMapping("/name")
    public Result<FamilyDTO> updateFamilyName(@RequestBody UpdateFamilyRequest request) {
        if (request == null || request.getName() == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "请输入家庭名称");
        }
        FamilyDTO family = familyService.updateFamilyName(AuthContext.requireUuid(), request.getName());
        return Result.success("修改成功", family);
    }

    @GetMapping("/is-admin")
    public Result<Boolean> isFamilyAdmin() {
        return Result.success(familyService.isFamilyAdmin(AuthContext.requireUuid()));
    }
}
