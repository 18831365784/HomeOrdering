package com.homeordering.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 订单数据传输对象
 */
@Data
public class OrderDTO implements Serializable {
    
    private String remark;
    private Integer status;
    private List<OrderItemDTO> items;
}
