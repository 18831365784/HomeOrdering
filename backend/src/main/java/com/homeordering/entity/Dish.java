package com.homeordering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品实体类
 */
@Data
@TableName("dish")
public class Dish implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String imageUrl;

    private String description;

    private BigDecimal price;

    private String category;

    private Integer orderCount;

    private Integer status;

    private String extensions;

    private Integer sort;

    /**
     * 所属家庭ID
     */
    private Long familyId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}