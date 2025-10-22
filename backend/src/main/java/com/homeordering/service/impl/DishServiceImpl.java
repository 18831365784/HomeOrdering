package com.homeordering.service.impl;

import com.homeordering.entity.Dish;
import com.homeordering.mapper.DishMapper;
import com.homeordering.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜品服务实现类
 */
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Override
    public Long addDish(Dish dish) {
        dish.setStatus(1); // 默认启用
        dishMapper.insert(dish);
        return dish.getId();
    }

    @Override
    public Dish getDishById(Long id) {
        return dishMapper.selectById(id);
    }

    @Override
    public List<Dish> getDishList(Integer status) {
        return dishMapper.selectList(status);
    }

    @Override
    public boolean updateDish(Dish dish) {
        return dishMapper.update(dish) > 0;
    }

    @Override
    public boolean deleteDish(Long id) {
        return dishMapper.deleteById(id) > 0;
    }
}
