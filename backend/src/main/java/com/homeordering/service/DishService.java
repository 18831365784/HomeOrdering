package com.homeordering.service;

import com.homeordering.entity.Dish;

import java.util.List;

public interface DishService {

    Long addDish(Dish dish);

    Dish getDishById(Long id);

    List<Dish> getDishList(Integer status);

    void updateDish(Dish dish);

    void deleteDish(Long id);

    void increaseOrderCount(Long dishId, Integer count);
}
