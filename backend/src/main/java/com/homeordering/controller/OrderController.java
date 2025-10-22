package com.homeordering.controller;

import com.homeordering.common.Result;
import com.homeordering.dto.OrderDTO;
import com.homeordering.service.OrderService;
import com.homeordering.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    public Result<Long> createOrder(@RequestBody OrderDTO orderDTO) {
        try {
            Long orderId = orderService.createOrder(orderDTO);
            return Result.success("订单创建成功", orderId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("订单创建失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询订单
     */
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById(@PathVariable Long id) {
        try {
            OrderVO orderVO = orderService.getOrderById(id);
            if (orderVO == null) {
                return Result.error("订单不存在");
            }
            return Result.success(orderVO);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询订单列表
     */
    @GetMapping("/list")
    public Result<List<OrderVO>> getOrderList(@RequestParam(required = false) Integer status) {
        try {
            List<OrderVO> orders = orderService.getOrderList(status);
            return Result.success(orders);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateOrderStatus(@PathVariable Long id, @RequestBody OrderDTO orderDTO) {
        try {
            boolean success = orderService.updateOrderStatus(id, orderDTO.getStatus());
            if (success) {
                return Result.success("订单状态更新成功");
            } else {
                return Result.error("订单状态更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("订单状态更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@PathVariable Long id) {
        try {
            boolean success = orderService.deleteOrder(id);
            if (success) {
                return Result.success("订单删除成功");
            } else {
                return Result.error("订单删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("订单删除失败：" + e.getMessage());
        }
    }
}
