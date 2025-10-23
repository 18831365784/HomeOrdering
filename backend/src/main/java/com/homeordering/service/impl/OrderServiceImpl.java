package com.homeordering.service.impl;

import com.homeordering.dto.OrderDTO;
import com.homeordering.dto.OrderItemDTO;
import com.homeordering.entity.Dish;
import com.homeordering.entity.Order;
import com.homeordering.entity.OrderDetail;
import com.homeordering.mapper.DishMapper;
import com.homeordering.mapper.OrderDetailMapper;
import com.homeordering.mapper.OrderMapper;
import com.homeordering.service.OrderService;
import com.homeordering.vo.OrderDetailVO;
import com.homeordering.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderDTO orderDTO) {
        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setStatus(0); // 待确认
        order.setRemark(orderDTO.getRemark());
        order.setTotalAmount(BigDecimal.ZERO);

        // 计算订单总金额并创建订单详情
        List<OrderDetail> orderDetails = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemDTO item : orderDTO.getItems()) {
            Dish dish = dishMapper.selectById(item.getDishId());
            if (dish == null) {
                throw new RuntimeException("菜品不存在：" + item.getDishId());
            }

            OrderDetail detail = new OrderDetail();
            detail.setDishId(dish.getId());
            detail.setDishName(dish.getName());
            detail.setDishImage(dish.getImageUrl());
            detail.setDishPrice(dish.getPrice());
            detail.setQuantity(item.getQuantity());
            detail.setSubtotal(dish.getPrice().multiply(new BigDecimal(item.getQuantity())));

            orderDetails.add(detail);
            totalAmount = totalAmount.add(detail.getSubtotal());

            // 增加菜品点单次数
            dishMapper.increaseOrderCount(dish.getId(), item.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        
        // 设置创建时间和更新时间（使用北京时间）
        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);
        
        orderMapper.insert(order);

        // 保存订单详情
        for (OrderDetail detail : orderDetails) {
            detail.setOrderId(order.getId());
        }
        orderDetailMapper.insertBatch(orderDetails);

        return order.getId();
    }

    @Override
    public OrderVO getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return null;
        }

        OrderVO orderVO = convertToVO(order);
        List<OrderDetail> details = orderDetailMapper.selectByOrderId(id);
        List<OrderDetailVO> detailVOs = new ArrayList<>();
        for (OrderDetail detail : details) {
            OrderDetailVO detailVO = new OrderDetailVO();
            BeanUtils.copyProperties(detail, detailVO);
            detailVOs.add(detailVO);
        }
        orderVO.setDetails(detailVOs);

        return orderVO;
    }

    @Override
    public List<OrderVO> getOrderList(Integer status) {
        List<Order> orders = orderMapper.selectList(status);
        List<OrderVO> orderVOs = new ArrayList<>();

        for (Order order : orders) {
            OrderVO orderVO = convertToVO(order);
            List<OrderDetail> details = orderDetailMapper.selectByOrderId(order.getId());
            List<OrderDetailVO> detailVOs = new ArrayList<>();
            for (OrderDetail detail : details) {
                OrderDetailVO detailVO = new OrderDetailVO();
                BeanUtils.copyProperties(detail, detailVO);
                detailVOs.add(detailVO);
            }
            orderVO.setDetails(detailVOs);
            orderVOs.add(orderVO);
        }

        return orderVOs;
    }

    @Override
    public boolean updateOrderStatus(Long id, Integer status) {
        // 创建更新参数对象
        Order updateOrder = new Order();
        updateOrder.setId(id);
        updateOrder.setStatus(status);
        updateOrder.setUpdateTime(LocalDateTime.now());
        
        return orderMapper.updateStatus(id, status, updateOrder.getUpdateTime()) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long id) {
        orderDetailMapper.deleteByOrderId(id);
        return orderMapper.deleteById(id) > 0;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
        return timeStr + randomStr;
    }

    /**
     * 转换为VO对象
     */
    private OrderVO convertToVO(Order order) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        
        // 设置状态文本
        switch (order.getStatus()) {
            case 0:
                orderVO.setStatusText("待确认");
                break;
            case 1:
                orderVO.setStatusText("已确认");
                break;
            case 2:
                orderVO.setStatusText("已完成");
                break;
            default:
                orderVO.setStatusText("未知状态");
        }
        
        return orderVO;
    }
}
