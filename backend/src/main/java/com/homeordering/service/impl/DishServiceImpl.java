package com.homeordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.homeordering.entity.Dish;
import com.homeordering.mapper.DishMapper;
import com.homeordering.service.DishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜品服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;

    @Override
    public Long addDish(Dish dish) {
        dish.setStatus(1);
        dish.setSort(0);
        dishMapper.insert(dish);
        log.info("新增菜品成功: id={}, name={}", dish.getId(), dish.getName());
        return dish.getId();
    }

    @Override
    public Dish getDishById(Long id) {
        return dishMapper.selectById(id);
    }

    @Override
    public List<Dish> getDishList(Long familyId, Integer status) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 按家庭ID过滤
        if (familyId != null) {
            queryWrapper.eq(Dish::getFamilyId, familyId);
        }
        if (status != null) {
            queryWrapper.eq(Dish::getStatus, status);
        }
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getCreateTime);
        return dishMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updateDish(Dish dish) {
        log.info("更新菜品: id={}", dish.getId());
        return dishMapper.updateById(dish) > 0;
    }

    @Override
    public boolean deleteDish(Long id) {
        log.info("删除菜品: id={}", id);
        return dishMapper.deleteById(id) > 0;
    }

    @Override
    public void increaseOrderCount(Long dishId, Integer count) {
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Dish::getId, dishId)
                .setSql("order_count = order_count + " + count);
        dishMapper.update(null, updateWrapper);
    }
}
