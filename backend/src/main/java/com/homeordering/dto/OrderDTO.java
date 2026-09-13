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

    /**
     * 家庭ID
     */
    private Long familyId;
}
