package com.homeordering.controller;

import com.homeordering.common.Result;
import com.homeordering.dto.OrderDTO;
import com.homeordering.service.OrderService;
import com.homeordering.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<Long> createOrder(@RequestBody OrderDTO orderDTO) {
        return Result.success("订单创建成功", orderService.createOrder(orderDTO));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById(@PathVariable Long id) {
        return Result.success(orderService.getOrderById(id));
    }

    @GetMapping("/list/my")
    public Result<List<OrderVO>> getMyOrders() {
        return Result.success(orderService.getMyOrders());
    }

    @GetMapping("/list/making")
    public Result<List<OrderVO>> getMyMakingOrders() {
        return Result.success(orderService.getMyMakingOrders());
    }

    @GetMapping("/list")
    public Result<List<OrderVO>> getOrderList(@RequestParam(required = false) Integer status) {
        return Result.success(orderService.getOrderList(status));
    }

    @PutMapping("/{id}/accept")
    public Result<String> acceptOrder(@PathVariable Long id) {
        orderService.acceptOrder(id);
        return Result.success("接单成功");
    }

    @PutMapping("/{id}/reject")
    public Result<String> rejectOrder(@PathVariable Long id) {
        orderService.rejectOrder(id);
        return Result.success("已拒绝订单");
    }

    @PutMapping("/{id}/finish")
    public Result<String> finishOrder(@PathVariable Long id) {
        orderService.finishOrder(id);
        return Result.success("订单已完成");
    }

    @PutMapping("/{id}/cancel")
    public Result<String> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success("订单已取消");
    }
}
