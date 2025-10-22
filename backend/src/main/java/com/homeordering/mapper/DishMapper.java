package com.homeordering.mapper;

import com.homeordering.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜品Mapper接口
 */
@Mapper
public interface DishMapper {

    /**
     * 新增菜品
     */
    int insert(Dish dish);

    /**
     * 根据ID查询菜品
     */
    Dish selectById(Long id);

    /**
     * 查询所有菜品
     */
    List<Dish> selectList(@Param("status") Integer status);

    /**
     * 更新菜品
     */
    int update(Dish dish);

    /**
     * 删除菜品
     */
    int deleteById(Long id);

    /**
     * 增加点单次数
     */
    int increaseOrderCount(@Param("dishId") Long dishId, @Param("count") Integer count);
}
