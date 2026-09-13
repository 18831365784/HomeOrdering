package com.homeordering.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    /**
     * 下单人UUID
     */
    private String customerUuid;

    /**
     * 下单客户昵称
     */
    private String customerName;

    /**
     * 制作者UUID
     */
    private String makerUuid;

    /**
     * 制作者昵称
     */
    private String makerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    private List<OrderDetailVO> details;
}
