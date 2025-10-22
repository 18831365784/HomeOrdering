package com.homeordering.service;

import com.homeordering.entity.Category;

import java.util.List;

public interface CategoryService {
    Long addCategory(Category category);
    Category getById(Long id);
    List<Category> list(Integer status);
    boolean updateCategory(Category category);
    boolean deleteById(Long id);
}


