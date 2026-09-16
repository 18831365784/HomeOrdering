package com.homeordering.service;

import com.homeordering.entity.Category;

import java.util.List;

public interface CategoryService {
    Long addCategory(Category category);
    List<Category> list(Integer status);
    void updateCategory(Category category);
    void deleteById(Long id);
}
