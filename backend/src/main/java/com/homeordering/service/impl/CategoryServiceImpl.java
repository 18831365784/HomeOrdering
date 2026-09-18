package com.homeordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homeordering.common.AuthContext;
import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.entity.Category;
import com.homeordering.entity.Family;
import com.homeordering.entity.User;
import com.homeordering.mapper.CategoryMapper;
import com.homeordering.service.CategoryService;
import com.homeordering.service.FamilyAccessService;
import com.homeordering.util.FileUrlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final FamilyAccessService familyAccessService;
    private final FileUrlHelper fileUrlHelper;

    @Override
    public Long addCategory(Category category) {
        User user = AuthContext.requireUser();
        Family family = familyAccessService.requireAdmin(user.getUuid());
        if (category.getName() == null || category.getName().isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID);
        }
        ensureUniqueName(family.getId(), category.getName().trim(), null);
        category.setName(category.getName().trim());
        category.setIconUrl(fileUrlHelper.toStoredPath(category.getIconUrl()));
        category.setFamilyId(family.getId());
        category.setStatus(category.getStatus() == null ? 1 : category.getStatus());
        category.setSort(category.getSort() == null ? 0 : category.getSort());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public List<Category> list(Integer status) {
        User user = AuthContext.requireUser();
        if (user.getFamilyId() == null) {
            return List.of();
        }
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getFamilyId, user.getFamilyId());
        if (status != null) {
            queryWrapper.eq(Category::getStatus, status);
        }
        queryWrapper.orderByAsc(Category::getSort);
        return categoryMapper.selectList(queryWrapper).stream().map(this::withPublicUrls).toList();
    }

    @Override
    public void updateCategory(Category category) {
        User user = AuthContext.requireUser();
        familyAccessService.requireAdmin(user.getUuid());
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (user.getFamilyId() == null || !user.getFamilyId().equals(existing.getFamilyId())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }
        if (category.getName() != null && !category.getName().isBlank()) {
            String name = category.getName().trim();
            ensureUniqueName(existing.getFamilyId(), name, existing.getId());
            category.setName(name);
        }
        category.setIconUrl(fileUrlHelper.toStoredPath(category.getIconUrl()));
        category.setFamilyId(existing.getFamilyId());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteById(Long id) {
        User user = AuthContext.requireUser();
        familyAccessService.requireAdmin(user.getUuid());
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (user.getFamilyId() == null || !user.getFamilyId().equals(existing.getFamilyId())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }
        categoryMapper.deleteById(id);
    }

    private void ensureUniqueName(Long familyId, String name, Long excludeId) {
        LambdaQueryWrapper<Category> query = new LambdaQueryWrapper<>();
        query.eq(Category::getFamilyId, familyId).eq(Category::getName, name);
        if (excludeId != null) {
            query.ne(Category::getId, excludeId);
        }
        if (categoryMapper.selectCount(query) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_EXISTS);
        }
    }

    private Category withPublicUrls(Category category) {
        category.setIconUrl(fileUrlHelper.toPublicUrl(category.getIconUrl()));
        return category;
    }
}
