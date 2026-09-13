package com.homeordering.service;

import com.homeordering.entity.Dish;

import java.util.List;

/**
 * 菜品服务接口
 */
public interface DishService {

    Long addDish(Dish dish);

    Dish getDishById(Long id);

    /**
     * 获取菜品列表（按家庭过滤）
     * @param familyId 家庭ID
     * @param status 状态筛选，null表示不筛选
     */
    List<Dish> getDishList(Long familyId, Integer status);

    boolean updateDish(Dish dish);

    boolean deleteDish(Long id);

    void increaseOrderCount(Long dishId, Integer count);
}
