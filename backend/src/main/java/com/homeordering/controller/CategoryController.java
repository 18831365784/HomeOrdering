package com.homeordering.controller;

import com.homeordering.common.Result;
import com.homeordering.entity.Category;
import com.homeordering.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品分类控制器
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public Result<Long> add(@RequestBody Category category) {
        try {
            Long id = categoryService.addCategory(category);
            return Result.success("分类添加成功", id);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("分类添加失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Category> get(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        return Result.success(category);
    }

    @GetMapping("/list")
    public Result<List<Category>> list(@RequestParam(required = false) Integer status) {
        List<Category> list = categoryService.list(status);
        return Result.success(list);
    }

    @PutMapping
    public Result<String> update(@RequestBody Category category) {
        boolean ok = categoryService.updateCategory(category);
        return ok ? Result.success("分类更新成功") : Result.error("分类更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean ok = categoryService.deleteById(id);
        return ok ? Result.success("分类删除成功") : Result.error("分类删除失败");
    }
}


