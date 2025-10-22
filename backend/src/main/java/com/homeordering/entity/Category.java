package com.homeordering.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜品分类实体
 */
@Data
public class Category {
    private Long id;
    private String name;
    private String iconUrl;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


