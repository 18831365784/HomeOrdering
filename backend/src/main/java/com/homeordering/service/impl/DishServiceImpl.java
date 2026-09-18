package com.homeordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.homeordering.common.AuthContext;
import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.entity.Dish;
import com.homeordering.entity.Family;
import com.homeordering.entity.User;
import com.homeordering.mapper.DishMapper;
import com.homeordering.service.DishService;
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
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final FamilyAccessService familyAccessService;
    private final FileUrlHelper fileUrlHelper;

    @Override
    public Long addDish(Dish dish) {
        User user = AuthContext.requireUser();
        Family family = familyAccessService.requireAdmin(user.getUuid());
        if (dish.getName() == null || dish.getName().isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID);
        }
        dish.setName(dish.getName().trim());
        dish.setImageUrl(fileUrlHelper.toStoredPath(dish.getImageUrl()));
        dish.setFamilyId(family.getId());
        dish.setStatus(dish.getStatus() == null ? 1 : dish.getStatus());
        dish.setSort(dish.getSort() == null ? 0 : dish.getSort());
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dishMapper.insert(dish);
        return dish.getId();
    }

    @Override
    public Dish getDishById(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException(ErrorCode.DISH_NOT_FOUND);
        }
        User user = AuthContext.requireUser();
        if (user.getFamilyId() == null || !user.getFamilyId().equals(dish.getFamilyId())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_MEMBER);
        }
        return withPublicUrls(dish);
    }

    @Override
    public List<Dish> getDishList(Integer status) {
        User user = AuthContext.requireUser();
        if (user.getFamilyId() == null) {
            return List.of();
        }
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getFamilyId, user.getFamilyId());
        if (status != null) {
            queryWrapper.eq(Dish::getStatus, status);
        }
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getCreateTime);
        return dishMapper.selectList(queryWrapper).stream().map(this::withPublicUrls).toList();
    }

    @Override
    public void updateDish(Dish dish) {
        User user = AuthContext.requireUser();
        familyAccessService.requireAdmin(user.getUuid());
        Dish existing = dishMapper.selectById(dish.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.DISH_NOT_FOUND);
        }
        if (!user.getFamilyId().equals(existing.getFamilyId())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }
        dish.setImageUrl(fileUrlHelper.toStoredPath(dish.getImageUrl()));
        dish.setFamilyId(existing.getFamilyId());
        dish.setUpdateTime(LocalDateTime.now());
        dishMapper.updateById(dish);
    }

    @Override
    public void deleteDish(Long id) {
        User user = AuthContext.requireUser();
        familyAccessService.requireAdmin(user.getUuid());
        Dish existing = dishMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DISH_NOT_FOUND);
        }
        if (!user.getFamilyId().equals(existing.getFamilyId())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }
        dishMapper.deleteById(id);
    }

    @Override
    public void increaseOrderCount(Long dishId, Integer count) {
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Dish::getId, dishId).setSql("order_count = order_count + " + count);
        dishMapper.update(null, updateWrapper);
    }

    private Dish withPublicUrls(Dish dish) {
        dish.setImageUrl(fileUrlHelper.toPublicUrl(dish.getImageUrl()));
        return dish;
    }
}
