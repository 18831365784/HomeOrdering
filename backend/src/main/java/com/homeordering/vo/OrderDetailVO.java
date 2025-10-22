package com.homeordering.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单详情视图对象
 */
@Data
public class OrderDetailVO implements Serializable {
    
    private Long dishId;
    private String dishName;
    private String dishImage;
    private BigDecimal dishPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
