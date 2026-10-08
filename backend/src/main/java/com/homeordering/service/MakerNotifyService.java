package com.homeordering.service;

public interface MakerNotifyService {

    /**
     * 订单已提交后通知制作人。失败不影响下单。
     */
    void notifyNewOrder(Long orderId);
}
