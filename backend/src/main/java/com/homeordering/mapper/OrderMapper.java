package com.homeordering.mapper;

import com.homeordering.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper {

    /**
     * 新增订单
     */
    int insert(Order order);

    /**
     * 根据ID查询订单
     */
    Order selectById(Long id);

    /**
     * 查询订单列表
     */
    List<Order> selectList(@Param("status") Integer status);

    /**
     * 更新订单状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 删除订单
     */
    int deleteById(Long id);
}
