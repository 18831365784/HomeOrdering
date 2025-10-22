package com.homeordering.service;

import com.homeordering.entity.Dish;

import java.util.List;

/**
 * 菜品服务接口
 */
public interface DishService {

    /**
     * 新增菜品
     */
    Long addDish(Dish dish);

    /**
     * 根据ID查询菜品
     */
    Dish getDishById(Long id);

    /**
     * 查询菜品列表
     */
    List<Dish> getDishList(Integer status);

    /**
     * 更新菜品
     */
    boolean updateDish(Dish dish);

    /**
     * 删除菜品
     */
    boolean deleteDish(Long id);
}
