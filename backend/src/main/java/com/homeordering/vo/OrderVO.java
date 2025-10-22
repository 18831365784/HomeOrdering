package com.homeordering.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图对象
 */
@Data
public class OrderVO implements Serializable {
    
    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;
    private String remark;
    private LocalDateTime createTime;
    private List<OrderDetailVO> details;
}
