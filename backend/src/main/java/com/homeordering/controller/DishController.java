package com.homeordering.controller;

import com.homeordering.common.Result;
import com.homeordering.entity.Dish;
import com.homeordering.service.DishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @PostMapping
    public Result<Long> addDish(@RequestBody Dish dish) {
        return Result.success("菜品添加成功", dishService.addDish(dish));
    }

    @GetMapping("/{id}")
    public Result<Dish> getDishById(@PathVariable Long id) {
        return Result.success(dishService.getDishById(id));
    }

    @GetMapping("/list")
    public Result<List<Dish>> getDishList(@RequestParam(required = false) Integer status) {
        return Result.success(dishService.getDishList(status));
    }

    @PutMapping
    public Result<String> updateDish(@RequestBody Dish dish) {
        dishService.updateDish(dish);
        return Result.success("菜品更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
        return Result.success("菜品删除成功");
    }
}
