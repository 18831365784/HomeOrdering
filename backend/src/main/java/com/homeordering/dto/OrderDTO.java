package com.homeordering.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建订单请求。下单人/家庭以登录态为准，客户端不可伪造。
 */
@Data
public class OrderDTO implements Serializable {

    private String remark;
    private List<OrderItemDTO> items;

    /** 制作人 UUID（须为同家庭成员） */
    private String makerUuid;
}
