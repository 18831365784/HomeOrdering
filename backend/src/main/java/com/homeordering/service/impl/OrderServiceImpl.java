package com.homeordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.homeordering.common.AuthContext;
import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.dto.OrderDTO;
import com.homeordering.dto.OrderItemDTO;
import com.homeordering.entity.Dish;
import com.homeordering.entity.Order;
import com.homeordering.entity.OrderDetail;
import com.homeordering.entity.User;
import com.homeordering.mapper.DishMapper;
import com.homeordering.mapper.OrderDetailMapper;
import com.homeordering.mapper.OrderMapper;
import com.homeordering.mapper.UserMapper;
import com.homeordering.service.FamilyAccessService;
import com.homeordering.service.OrderService;
import com.homeordering.vo.OrderDetailVO;
import com.homeordering.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final DishMapper dishMapper;
    private final UserMapper userMapper;
    private final FamilyAccessService familyAccessService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderDTO orderDTO) {
        User customer = AuthContext.requireUser();
        if (customer.getFamilyId() == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }
        if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_EMPTY);
        }
        if (orderDTO.getMakerUuid() == null || orderDTO.getMakerUuid().isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID);
        }
        User maker = familyAccessService.requireSameFamily(customer.getFamilyId(), orderDTO.getMakerUuid());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO item : orderDTO.getItems()) {
            Dish dish = dishMapper.selectById(item.getDishId());
            if (dish == null) {
                throw new BusinessException(ErrorCode.DISH_NOT_FOUND);
            }
            if (!customer.getFamilyId().equals(dish.getFamilyId())) {
                throw new BusinessException(ErrorCode.FAMILY_NOT_MEMBER);
            }
            BigDecimal actualUnitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : dish.getPrice();
            totalAmount = totalAmount.add(actualUnitPrice.multiply(new BigDecimal(item.getQuantity())));
        }

        if (customer.getBalance() == null || customer.getBalance().compareTo(totalAmount) < 0) {
            throw new BusinessException(ErrorCode.BALANCE_INSUFFICIENT);
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setStatus(0);
        order.setRemark(orderDTO.getRemark());
        order.setTotalAmount(totalAmount);
        order.setCustomerUuid(customer.getUuid());
        order.setCustomerName(customer.getNickname());
        order.setMakerUuid(maker.getUuid());
        order.setMakerName(maker.getNickname());
        order.setFamilyId(customer.getFamilyId());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUuid, customer.getUuid())
                .setSql("balance = balance - " + totalAmount);
        userMapper.update(null, updateWrapper);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (OrderItemDTO item : orderDTO.getItems()) {
            Dish dish = dishMapper.selectById(item.getDishId());
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(order.getId());
            detail.setDishId(dish.getId());
            detail.setDishName(dish.getName());
            detail.setDishImage(dish.getImageUrl());
            BigDecimal actualUnitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : dish.getPrice();
            detail.setDishPrice(actualUnitPrice);
            detail.setQuantity(item.getQuantity());
            detail.setSubtotal(actualUnitPrice.multiply(new BigDecimal(item.getQuantity())));
            orderDetails.add(detail);

            LambdaUpdateWrapper<Dish> dishUpdateWrapper = new LambdaUpdateWrapper<>();
            dishUpdateWrapper.eq(Dish::getId, dish.getId())
                    .setSql("order_count = order_count + " + item.getQuantity());
            dishMapper.update(null, dishUpdateWrapper);
        }
        for (OrderDetail detail : orderDetails) {
            orderDetailMapper.insert(detail);
        }
        return order.getId();
    }

    @Override
    public OrderVO getOrderById(Long id) {
        Order order = requireFamilyOrder(id);
        OrderVO orderVO = convertToVO(order);
        orderVO.setDetails(loadDetails(id));
        return orderVO;
    }

    @Override
    public List<OrderVO> getOrderList(Integer status) {
        User user = AuthContext.requireUser();
        if (user.getFamilyId() == null) {
            return List.of();
        }
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getFamilyId, user.getFamilyId());
        if (status != null) {
            queryWrapper.eq(Order::getStatus, status);
        }
        queryWrapper.orderByDesc(Order::getCreateTime);
        return convertToVOList(orderMapper.selectList(queryWrapper));
    }

    @Override
    public List<OrderVO> getMyOrders() {
        User user = AuthContext.requireUser();
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getCustomerUuid, user.getUuid());
        if (user.getFamilyId() != null) {
            queryWrapper.eq(Order::getFamilyId, user.getFamilyId());
        }
        queryWrapper.orderByDesc(Order::getCreateTime);
        return convertToVOList(orderMapper.selectList(queryWrapper));
    }

    @Override
    public List<OrderVO> getMyMakingOrders() {
        User user = AuthContext.requireUser();
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getMakerUuid, user.getUuid());
        if (user.getFamilyId() != null) {
            queryWrapper.eq(Order::getFamilyId, user.getFamilyId());
        }
        queryWrapper.orderByDesc(Order::getCreateTime);
        return convertToVOList(orderMapper.selectList(queryWrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptOrder(Long id) {
        User actor = AuthContext.requireUser();
        Order order = requireFamilyOrder(id);
        if (!actor.getUuid().equals(order.getMakerUuid())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_MAKER);
        }
        if (order.getStatus() == null || order.getStatus() != 0) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.setStatus(1);
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finishOrder(Long id) {
        User actor = AuthContext.requireUser();
        Order order = requireFamilyOrder(id);
        if (!actor.getUuid().equals(order.getMakerUuid())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_MAKER);
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.setStatus(2);
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long id) {
        User actor = AuthContext.requireUser();
        Order order = requireFamilyOrder(id);
        if (!actor.getUuid().equals(order.getCustomerUuid())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_MAKER.getCode(), "只能取消自己的订单");
        }
        if (order.getStatus() == null || order.getStatus() != 0) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUuid, order.getCustomerUuid())
                .setSql("balance = balance + " + order.getTotalAmount());
        userMapper.update(null, updateWrapper);
        order.setStatus(-1);
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    private Order requireFamilyOrder(Long id) {
        User user = AuthContext.requireUser();
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (user.getFamilyId() == null || !user.getFamilyId().equals(order.getFamilyId())) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_MEMBER);
        }
        return order;
    }

    private String generateOrderNo() {
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return timeStr + randomStr;
    }

    private OrderVO convertToVO(Order order) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        switch (order.getStatus() == null ? 99 : order.getStatus()) {
            case -1 -> orderVO.setStatusText("已取消");
            case 0 -> orderVO.setStatusText("待接单");
            case 1 -> orderVO.setStatusText("制作中");
            case 2 -> orderVO.setStatusText("已完成");
            default -> orderVO.setStatusText("未知状态");
        }
        return orderVO;
    }

    private List<OrderVO> convertToVOList(List<Order> orders) {
        List<OrderVO> orderVOs = new ArrayList<>();
        for (Order order : orders) {
            OrderVO orderVO = convertToVO(order);
            orderVO.setDetails(loadDetails(order.getId()));
            orderVOs.add(orderVO);
        }
        return orderVOs;
    }

    private List<OrderDetailVO> loadDetails(Long orderId) {
        List<OrderDetail> details = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));
        List<OrderDetailVO> detailVOs = new ArrayList<>();
        for (OrderDetail detail : details) {
            OrderDetailVO detailVO = new OrderDetailVO();
            BeanUtils.copyProperties(detail, detailVO);
            detailVOs.add(detailVO);
        }
        return detailVOs;
    }
}
