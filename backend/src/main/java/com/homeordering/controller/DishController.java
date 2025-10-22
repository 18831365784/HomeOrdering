package com.homeordering.controller;

import com.homeordering.common.Result;
import com.homeordering.entity.Dish;
import com.homeordering.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品控制器
 */
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     */
    @PostMapping
    public Result<Long> addDish(@RequestBody Dish dish) {
        try {
            Long id = dishService.addDish(dish);
            return Result.success("菜品添加成功", id);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("菜品添加失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询菜品
     */
    @GetMapping("/{id}")
    public Result<Dish> getDishById(@PathVariable Long id) {
        try {
            Dish dish = dishService.getDishById(id);
            if (dish == null) {
                return Result.error("菜品不存在");
            }
            return Result.success(dish);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询菜品列表
     */
    @GetMapping("/list")
    public Result<List<Dish>> getDishList(@RequestParam(required = false) Integer status) {
        try {
            List<Dish> dishes = dishService.getDishList(status);
            return Result.success(dishes);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 更新菜品
     */
    @PutMapping
    public Result<String> updateDish(@RequestBody Dish dish) {
        try {
            boolean success = dishService.updateDish(dish);
            if (success) {
                return Result.success("菜品更新成功");
            } else {
                return Result.error("菜品更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("菜品更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除菜品
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteDish(@PathVariable Long id) {
        try {
            boolean success = dishService.deleteDish(id);
            if (success) {
                return Result.success("菜品删除成功");
            } else {
                return Result.error("菜品删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("菜品删除失败：" + e.getMessage());
        }
    }
}
