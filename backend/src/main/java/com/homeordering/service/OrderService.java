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
     * 查询订单列表（全部）
     */
    List<OrderVO> getOrderList(Integer status);

    /**
     * 查询我的订单（我下的单）
     */
    List<OrderVO> getMyOrders(String uuid);

    /**
     * 查询我的制作（我作为制作者的订单）
     */
    List<OrderVO> getMyMakingOrders(String uuid);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long id, Integer status);

    /**
     * 制作者接单（状态0→1）
     */
    boolean acceptOrder(Long id, String makerUuid);

    /**
     * 制作者完成订单（状态1→2）
     */
    boolean finishOrder(Long id, String makerUuid);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long id, String customerUuid);

    /**
     * 删除订单
     */
    boolean deleteOrder(Long id);
}
