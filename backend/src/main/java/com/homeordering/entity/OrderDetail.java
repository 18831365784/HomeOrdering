package com.homeordering.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单详情实体类
 */
@Data
public class OrderDetail implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 菜品ID
     */
    private Long dishId;

    /**
     * 菜品名称(冗余字段,防止菜品被删除)
     */
    private String dishName;

    /**
     * 菜品图片
     */
    private String dishImage;

    /**
     * 菜品价格(下单时价格)
     */
    private BigDecimal dishPrice;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 小计金额
     */
    private BigDecimal subtotal;
}
