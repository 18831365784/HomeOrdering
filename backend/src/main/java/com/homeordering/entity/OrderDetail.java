package com.homeordering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单详情实体类
 */
@Data
@TableName("order_detail")
public class OrderDetail implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long dishId;

    private String dishName;

    private String dishImage;

    private BigDecimal dishPrice;

    private Integer quantity;

    private BigDecimal subtotal;
}