package com.homeordering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜品分类实体
 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String iconUrl;

    private Integer sort;

    private Integer status;

    /**
     * 所属家庭ID
     */
    private Long familyId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}