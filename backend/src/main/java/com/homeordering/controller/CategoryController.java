package com.homeordering.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.common.Result;
import com.homeordering.entity.Category;
import com.homeordering.entity.Family;
import com.homeordering.entity.User;
import com.homeordering.mapper.CategoryMapper;
import com.homeordering.mapper.FamilyMapper;
import com.homeordering.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类控制器
 */
@Slf4j
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final FamilyMapper familyMapper;

    @GetMapping("/list")
    public Result<List<Category>> getCategoryList(@RequestParam(required = false) Integer status, @RequestParam String uuid) {
        User user = getUserByUuid(uuid);
        if (user.getFamilyId() == null) {
            return Result.success(List.of()); // 没有家庭，返回空列表
        }

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getFamilyId, user.getFamilyId());
        if (status != null) {
            queryWrapper.eq(Category::getStatus, status);
        }
        queryWrapper.orderByAsc(Category::getSort);
        List<Category> categories = categoryMapper.selectList(queryWrapper);
        return Result.success(categories);
    }

    @PostMapping
    public Result<Long> addCategory(@RequestBody Category category, @RequestParam String uuid) {
        // 检查用户是否是家庭管理员
        User user = getUserByUuid(uuid);
        if (user.getFamilyId() == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }
        Family family = familyMapper.selectById(user.getFamilyId());
        if (!family.getAdminUuid().equals(uuid)) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }

        log.info("新增分类: {}", category.getName());

        category.setFamilyId(user.getFamilyId());
        category.setStatus(1);
        category.setSort(0);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.insert(category);

        return Result.success("分类添加成功", category.getId());
    }

    @PutMapping
    public Result<String> updateCategory(@RequestBody Category category, @RequestParam String uuid) {
        // 检查用户是否是家庭管理员
        User user = getUserByUuid(uuid);
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        // 只能修改自己家庭的分类
        if (user.getFamilyId() == null || !user.getFamilyId().equals(existing.getFamilyId())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }

        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(category);

        return Result.success("分类更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Long id, @RequestParam String uuid) {
        // 检查用户是否是家庭管理员
        User user = getUserByUuid(uuid);
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        // 只能删除自己家庭的分类
        if (user.getFamilyId() == null || !user.getFamilyId().equals(existing.getFamilyId())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }

        categoryMapper.deleteById(id);
        return Result.success("分类删除成功");
    }

    private User getUserByUuid(String uuid) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUuid, uuid)
        );
    }
}
