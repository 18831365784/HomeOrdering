package com.homeordering.service;

import com.homeordering.dto.OrderDTO;
import com.homeordering.vo.OrderVO;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    Long createOrder(OrderDTO orderDTO);

    /**
     * 根据ID查询订单
     */
    OrderVO getOrderById(Long id);

    /**
     * 查询订单列表
     */
    List<OrderVO> getOrderList(Integer status);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long id, Integer status);

    /**
     * 删除订单
     */
    boolean deleteOrder(Long id);
}
