package com.homeordering.mapper;

import com.homeordering.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单详情Mapper接口
 */
@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单详情
     */
    int insertBatch(List<OrderDetail> orderDetails);

    /**
     * 根据订单ID查询订单详情
     */
    List<OrderDetail> selectByOrderId(Long orderId);

    /**
     * 根据订单ID删除订单详情
     */
    int deleteByOrderId(Long orderId);
}
