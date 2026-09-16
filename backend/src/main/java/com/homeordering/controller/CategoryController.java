package com.homeordering.controller;

import com.homeordering.common.Result;
import com.homeordering.entity.Category;
import com.homeordering.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> getCategoryList(@RequestParam(required = false) Integer status) {
        return Result.success(categoryService.list(status));
    }

    @PostMapping
    public Result<Long> addCategory(@RequestBody Category category) {
        return Result.success("分类添加成功", categoryService.addCategory(category));
    }

    @PutMapping
    public Result<String> updateCategory(@RequestBody Category category) {
        categoryService.updateCategory(category);
        return Result.success("分类更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteById(id);
        return Result.success("分类删除成功");
    }
}
