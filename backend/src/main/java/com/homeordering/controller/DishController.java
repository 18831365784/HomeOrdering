package com.homeordering.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.common.Result;
import com.homeordering.entity.Dish;
import com.homeordering.entity.Family;
import com.homeordering.entity.User;
import com.homeordering.mapper.DishMapper;
import com.homeordering.mapper.FamilyMapper;
import com.homeordering.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜品控制器
 */
@Slf4j
@RestController
@RequestMapping("/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishMapper dishMapper;
    private final UserMapper userMapper;
    private final FamilyMapper familyMapper;

    @PostMapping
    public Result<Long> addDish(@RequestBody Dish dish, @RequestParam String uuid) {
        // 检查用户是否是家庭管理员
        User user = getUserByUuid(uuid);
        if (user.getFamilyId() == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }
        Family family = familyMapper.selectById(user.getFamilyId());
        if (!family.getAdminUuid().equals(uuid)) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }

        log.info("新增菜品: {}", dish.getName());

        dish.setFamilyId(user.getFamilyId());
        dish.setStatus(1);
        dish.setSort(0);
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dishMapper.insert(dish);

        return Result.success("菜品添加成功", dish.getId());
    }

    @GetMapping("/{id}")
    public Result<Dish> getDishById(@PathVariable Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException(ErrorCode.DISH_NOT_FOUND);
        }
        return Result.success(dish);
    }

    @GetMapping("/list")
    public Result<List<Dish>> getDishList(@RequestParam(required = false) Integer status, @RequestParam String uuid) {
        User user = getUserByUuid(uuid);
        if (user.getFamilyId() == null) {
            return Result.success(List.of()); // 没有家庭，返回空列表
        }

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getFamilyId, user.getFamilyId());
        if (status != null) {
            queryWrapper.eq(Dish::getStatus, status);
        }
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getCreateTime);
        List<Dish> dishes = dishMapper.selectList(queryWrapper);
        return Result.success(dishes);
    }

    @PutMapping
    public Result<String> updateDish(@RequestBody Dish dish, @RequestParam String uuid) {
        // 检查用户是否是家庭管理员
        User user = getUserByUuid(uuid);
        Dish existing = dishMapper.selectById(dish.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.DISH_NOT_FOUND);
        }
        // 只能修改自己家庭的菜品
        if (user.getFamilyId() == null || !user.getFamilyId().equals(existing.getFamilyId())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }

        dish.setUpdateTime(LocalDateTime.now());
        dishMapper.updateById(dish);

        return Result.success("菜品更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDish(@PathVariable Long id, @RequestParam String uuid) {
        // 检查用户是否是家庭管理员
        User user = getUserByUuid(uuid);
        Dish existing = dishMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DISH_NOT_FOUND);
        }
        // 只能删除自己家庭的菜品
        if (user.getFamilyId() == null || !user.getFamilyId().equals(existing.getFamilyId())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_ADMIN);
        }

        dishMapper.deleteById(id);
        return Result.success("菜品删除成功");
    }

    private User getUserByUuid(String uuid) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUuid, uuid)
        );
    }
}
