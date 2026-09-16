package com.homeordering.service;

import com.homeordering.dto.OrderDTO;
import com.homeordering.vo.OrderVO;

import java.util.List;

public interface OrderService {

    Long createOrder(OrderDTO orderDTO);

    OrderVO getOrderById(Long id);

    List<OrderVO> getOrderList(Integer status);

    List<OrderVO> getMyOrders();

    List<OrderVO> getMyMakingOrders();

    boolean acceptOrder(Long id);

    boolean finishOrder(Long id);

    boolean cancelOrder(Long id);
}
