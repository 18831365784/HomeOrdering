package com.homeordering.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 菜品数据传输对象
 */
@Data
public class DishDTO implements Serializable {
    
    private Long id;
    private String name;
    private String imageUrl;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer orderCount;
    private Integer status;
}
