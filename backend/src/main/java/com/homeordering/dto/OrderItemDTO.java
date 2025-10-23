package com.homeordering.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单项数据传输对象
 */
@Data
public class OrderItemDTO implements Serializable {
    
    private Long dishId;
    private Integer quantity;
    private BigDecimal unitPrice; // 包含选项价格的实际单价
}
