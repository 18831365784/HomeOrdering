package com.homeordering.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 订单项数据传输对象
 */
@Data
public class OrderItemDTO implements Serializable {
    
    private Long dishId;
    private Integer quantity;
}
