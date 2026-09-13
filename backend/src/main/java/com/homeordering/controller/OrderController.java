package com.homeordering.controller;

import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.common.Result;
import com.homeordering.dto.OrderDTO;
import com.homeordering.entity.Order;
import com.homeordering.mapper.OrderMapper;
import com.homeordering.service.OrderService;
import com.homeordering.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    /**
     * 创建订单
     */
    @PostMapping
    public Result<Long> createOrder(@RequestBody OrderDTO orderDTO) {
        log.info("创建订单: {} 个商品", orderDTO.getItems().size());

        Long orderId = orderService.createOrder(orderDTO);
        return Result.success("订单创建成功", orderId);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById(@PathVariable Long id) {
        OrderVO orderVO = orderService.getOrderById(id);
        if (orderVO == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return Result.success(orderVO);
    }

    /**
     * 获取我的订单（我下的单）
     */
    @GetMapping("/list/my")
    public Result<List<OrderVO>> getMyOrders(@RequestParam String uuid) {
        log.info("获取我的订单: uuid={}", uuid);
        List<OrderVO> orders = orderService.getMyOrders(uuid);
        return Result.success(orders);
    }

    /**
     * 获取我的制作（我作为制作者的订单）
     */
    @GetMapping("/list/making")
    public Result<List<OrderVO>> getMyMakingOrders(@RequestParam String uuid) {
        log.info("获取我的制作: uuid={}", uuid);
        List<OrderVO> orders = orderService.getMyMakingOrders(uuid);
        return Result.success(orders);
    }

    /**
     * 获取订单列表（全部）
     */
    @GetMapping("/list")
    public Result<List<OrderVO>> getOrderList(@RequestParam(required = false) Integer status) {
        List<OrderVO> orders = orderService.getOrderList(status);
        return Result.success(orders);
    }

    /**
     * 制作者接单（状态0→1）
     */
    @PutMapping("/{id}/accept")
    public Result<String> acceptOrder(@PathVariable Long id, @RequestParam String makerUuid) {
        log.info("制作者接单: id={}, makerUuid={}", id, makerUuid);
        orderService.acceptOrder(id, makerUuid);
        return Result.success("接单成功");
    }

    /**
     * 制作者完成订单（状态1→2）
     */
    @PutMapping("/{id}/finish")
    public Result<String> finishOrder(@PathVariable Long id, @RequestParam String makerUuid) {
        log.info("制作者完成订单: id={}, makerUuid={}", id, makerUuid);
        orderService.finishOrder(id, makerUuid);
        return Result.success("订单已完成");
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    public Result<String> cancelOrder(@PathVariable Long id, @RequestParam String customerUuid) {
        log.info("取消订单: id={}, customerUuid={}", id, customerUuid);
        orderService.cancelOrder(id, customerUuid);
        return Result.success("订单已取消");
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateOrderStatus(@PathVariable Long id, @RequestBody OrderDTO orderDTO) {
        log.info("更新订单状态: id={}, status={}", id, orderDTO.getStatus());

        Order existing = orderMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        orderService.updateOrderStatus(id, orderDTO.getStatus());
        return Result.success("订单状态更新成功");
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@PathVariable Long id) {
        log.info("删除订单: id={}", id);

        Order existing = orderMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        orderService.deleteOrder(id);
        return Result.success("订单删除成功");
    }
}
