package com.homeordering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("`order`")
public class Order implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private BigDecimal totalAmount;

    /**
     * 订单状态: 0-待接单 1-制作中 2-已完成 -1-已取消 -2-制作人已拒绝
     */
    private Integer status;

    private String remark;

    /**
     * 下单人UUID
     */
    private String customerUuid;

    /**
     * 下单客户昵称
     */
    private String customerName;

    /**
     * 制作者UUID
     */
    private String makerUuid;

    /**
     * 制作者昵称
     */
    private String makerName;

    /**
     * 家庭ID
     */
    private Long familyId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}