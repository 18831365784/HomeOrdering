package com.homeordering.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品实体类
 */
@Data
public class Dish implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 菜品名称
     */
    private String name;

    /**
     * 菜品图片URL
     */
    private String imageUrl;

    /**
     * 菜品简介
     */
    private String description;

    /**
     * 菜品价格
     */
    private BigDecimal price;

    /**
     * 菜品分类: 肉类、蔬菜、主食、凉菜、汤
     */
    private String category;

    /**
     * 点单次数
     */
    private Integer orderCount;

    /**
     * 状态: 0-停用 1-启用
     */
    private Integer status;

    /**
     * 排序，越小越靠前
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
