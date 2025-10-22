package com.homeordering.mapper;

import com.homeordering.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    int insert(Category category);
    Category selectById(@Param("id") Long id);
    List<Category> selectList(@Param("status") Integer status);
    int update(Category category);
    int deleteById(@Param("id") Long id);
}


