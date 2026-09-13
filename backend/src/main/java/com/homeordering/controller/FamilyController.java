package com.homeordering.controller;

import com.homeordering.common.Result;
import com.homeordering.dto.response.FamilyDTO;
import com.homeordering.dto.response.FamilyMemberDTO;
import com.homeordering.service.FamilyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 家庭控制器
 */
@Slf4j
@RestController
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    /**
     * 创建家庭
     */
    @PostMapping
    public Result<FamilyDTO> createFamily(@RequestBody Map<String, String> params) {
        String uuid = params.get("uuid");
        String name = params.get("name");
        log.info("创建家庭: uuid={}, name={}", uuid, name);
        FamilyDTO family = familyService.createFamily(uuid, name);
        return Result.success("创建成功", family);
    }

    /**
     * 通过邀请码加入家庭
     */
    @PostMapping("/join")
    public Result<FamilyDTO> joinFamily(@RequestBody Map<String, String> params) {
        String uuid = params.get("uuid");
        String inviteCode = params.get("inviteCode");
        log.info("加入家庭: uuid={}, inviteCode={}", uuid, inviteCode);
        FamilyDTO family = familyService.joinFamily(uuid, inviteCode);
        return Result.success("加入成功", family);
    }

    /**
     * 获取家庭信息
     */
    @GetMapping("/info")
    public Result<FamilyDTO> getFamilyInfo(@RequestParam String uuid) {
        log.info("获取家庭信息: uuid={}", uuid);
        FamilyDTO family = familyService.getFamilyInfo(uuid);
        return Result.success(family);
    }

    /**
     * 获取家庭成员列表
     */
    @GetMapping("/members")
    public Result<List<FamilyMemberDTO>> getFamilyMembers(@RequestParam String uuid) {
        log.info("获取家庭成员: uuid={}", uuid);
        List<FamilyMemberDTO> members = familyService.getFamilyMembers(uuid);
        return Result.success(members);
    }

    /**
     * 重新生成邀请码（管理员）
     */
    @PostMapping("/regenerate-code")
    public Result<String> regenerateInviteCode(@RequestParam String uuid) {
        log.info("重新生成邀请码: uuid={}", uuid);
        String newCode = familyService.regenerateInviteCode(uuid);
        return Result.success("生成成功", newCode);
    }

    /**
     * 判断是否是家庭管理员
     */
    @GetMapping("/is-admin")
    public Result<Boolean> isFamilyAdmin(@RequestParam String uuid) {
        boolean isAdmin = familyService.isFamilyAdmin(uuid);
        return Result.success(isAdmin);
    }
}
