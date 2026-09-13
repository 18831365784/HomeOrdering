package com.homeordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

/**
 * 订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final DishMapper dishMapper;
    private final com.homeordering.mapper.UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderDTO orderDTO) {
        log.info("创建订单: {} 个商品", orderDTO.getItems().size());

        // 1. 获取下单人并校验余额
        User customer = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUuid, orderDTO.getCustomerUuid()));
        if (customer == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO item : orderDTO.getItems()) {
            Dish dish = dishMapper.selectById(item.getDishId());
            if (dish == null) {
                throw new BusinessException(ErrorCode.DISH_NOT_FOUND);
            }
            BigDecimal actualUnitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : dish.getPrice();
            totalAmount = totalAmount.add(actualUnitPrice.multiply(new BigDecimal(item.getQuantity())));
        }

        // 校验余额是否充足
        if (customer.getBalance().compareTo(totalAmount) < 0) {
            throw new BusinessException(ErrorCode.BALANCE_INSUFFICIENT);
        }

        // 2. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setStatus(0);
        order.setRemark(orderDTO.getRemark());
        order.setTotalAmount(totalAmount);
        order.setCustomerUuid(orderDTO.getCustomerUuid());
        order.setCustomerName(orderDTO.getCustomerName());
        order.setMakerUuid(orderDTO.getMakerUuid());
        order.setMakerName(orderDTO.getMakerName());
        order.setFamilyId(orderDTO.getFamilyId());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);

        // 3. 扣减余额
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUuid, orderDTO.getCustomerUuid())
                .setSql("balance = balance - " + totalAmount);
        userMapper.update(null, updateWrapper);
        log.info("余额扣减成功: uuid={}, 扣减金额={}, 剩余余额={}", orderDTO.getCustomerUuid(), totalAmount, customer.getBalance().subtract(totalAmount));

        // 4. 保存订单明细
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

            // 增加菜品点单次数
            LambdaUpdateWrapper<Dish> dishUpdateWrapper = new LambdaUpdateWrapper<>();
            dishUpdateWrapper.eq(Dish::getId, dish.getId())
                    .setSql("order_count = order_count + " + item.getQuantity());
            dishMapper.update(null, dishUpdateWrapper);
        }

        for (OrderDetail detail : orderDetails) {
            orderDetailMapper.insert(detail);
        }

        log.info("订单创建成功: id={}, orderNo={}", order.getId(), order.getOrderNo());
        return order.getId();
    }

    @Override
    public OrderVO getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return null;
        }

        OrderVO orderVO = convertToVO(order);
        List<OrderDetail> details = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, id));
        orderVO.setDetails(convertDetailList(details));

        return orderVO;
    }

    @Override
    public List<OrderVO> getOrderList(Integer status) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(Order::getStatus, status);
        }
        queryWrapper.orderByDesc(Order::getCreateTime);

        List<Order> orders = orderMapper.selectList(queryWrapper);
        List<OrderVO> orderVOs = new ArrayList<>();

        for (Order order : orders) {
            OrderVO orderVO = convertToVO(order);
            List<OrderDetail> details = orderDetailMapper.selectList(
                    new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, order.getId()));
            orderVO.setDetails(convertDetailList(details));
            orderVOs.add(orderVO);
        }

        return orderVOs;
    }

    @Override
    public List<OrderVO> getMyOrders(String uuid) {
        // 根据uuid获取用户的familyId
        com.homeordering.entity.User user = getUserByUuid(uuid);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getCustomerUuid, uuid);
        // 如果用户有家庭，则只显示该家庭的订单
        if (user != null && user.getFamilyId() != null) {
            queryWrapper.eq(Order::getFamilyId, user.getFamilyId());
        }
        queryWrapper.orderByDesc(Order::getCreateTime);

        List<Order> orders = orderMapper.selectList(queryWrapper);
        return convertToVOList(orders);
    }

    @Override
    public List<OrderVO> getMyMakingOrders(String uuid) {
        // 根据uuid获取用户的familyId
        com.homeordering.entity.User user = getUserByUuid(uuid);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getMakerUuid, uuid);
        // 如果用户有家庭，则只显示该家庭的订单
        if (user != null && user.getFamilyId() != null) {
            queryWrapper.eq(Order::getFamilyId, user.getFamilyId());
        }
        queryWrapper.orderByDesc(Order::getCreateTime);

        List<Order> orders = orderMapper.selectList(queryWrapper);
        return convertToVOList(orders);
    }

    @Override
    public boolean updateOrderStatus(Long id, Integer status) {
        log.info("更新订单状态: id={}, status={}", id, status);

        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());

        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptOrder(Long id, String makerUuid) {
        log.info("制作者接单: id={}, makerUuid={}", id, makerUuid);

        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (!makerUuid.equals(order.getMakerUuid())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_MAKER);
        }

        if (order.getStatus() != 0) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        order.setStatus(1);
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finishOrder(Long id, String makerUuid) {
        log.info("制作者完成订单: id={}, makerUuid={}", id, makerUuid);

        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (!makerUuid.equals(order.getMakerUuid())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_MAKER);
        }

        if (order.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        order.setStatus(2);
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long id, String customerUuid) {
        log.info("取消订单: id={}, customerUuid={}", id, customerUuid);

        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (!customerUuid.equals(order.getCustomerUuid())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_MAKER);
        }

        if (order.getStatus() != 0) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }

        // 取消订单时退还余额
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUuid, order.getCustomerUuid())
                .setSql("balance = balance + " + order.getTotalAmount());
        userMapper.update(null, updateWrapper);
        log.info("取消订单余额退还: uuid={}, 退还金额={}", order.getCustomerUuid(), order.getTotalAmount());

        order.setStatus(-1);
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long id) {
        log.info("删除订单: id={}", id);

        orderDetailMapper.delete(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, id));
        return orderMapper.deleteById(id) > 0;
    }

    private String generateOrderNo() {
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return timeStr + randomStr;
    }

    private OrderVO convertToVO(Order order) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);

        switch (order.getStatus()) {
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
            List<OrderDetail> details = orderDetailMapper.selectList(
                    new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, order.getId()));
            orderVO.setDetails(convertDetailList(details));
            orderVOs.add(orderVO);
        }
        return orderVOs;
    }

    private List<OrderDetailVO> convertDetailList(List<OrderDetail> details) {
        List<OrderDetailVO> detailVOs = new ArrayList<>();
        for (OrderDetail detail : details) {
            OrderDetailVO detailVO = new OrderDetailVO();
            BeanUtils.copyProperties(detail, detailVO);
            detailVOs.add(detailVO);
        }
        return detailVOs;
    }

    private com.homeordering.entity.User getUserByUuid(String uuid) {
        if (uuid == null || userMapper == null) {
            return null;
        }
        return userMapper.selectOne(
            new LambdaQueryWrapper<com.homeordering.entity.User>()
                .eq(com.homeordering.entity.User::getUuid, uuid)
        );
    }
}
