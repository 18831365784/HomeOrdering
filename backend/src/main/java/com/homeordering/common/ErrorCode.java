package com.homeordering.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 系统错误 (1xxx)
    SYSTEM_ERROR(1000, "系统异常"),
    PARAM_INVALID(1001, "参数无效"),

    // 用户相关 (2xxx)
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_ALREADY_EXISTS(2002, "用户已存在"),
    USERNAME_PASSWORD_ERROR(2003, "用户名或密码错误"),
    TOKEN_EXPIRED(2004, "登录已过期"),
    TOKEN_INVALID(2005, "无效的登录凭证"),
    NOT_LOGIN(2006, "未登录"),

    // 菜品相关 (3xxx)
    DISH_NOT_FOUND(3001, "菜品不存在"),
    DISH_OFFLINE(3002, "菜品已下架"),

    // 分类相关 (4xxx)
    CATEGORY_NOT_FOUND(4001, "分类不存在"),
    CATEGORY_HAS_DISHES(4002, "分类下有菜品，无法删除"),
    CATEGORY_NAME_EXISTS(4003, "同家庭下分类名称已存在"),

    // 订单相关 (5xxx)
    ORDER_NOT_FOUND(5001, "订单不存在"),
    ORDER_STATUS_INVALID(5002, "订单状态无效"),
    ORDER_EMPTY(5003, "订单不能为空"),
    CART_EMPTY(5004, "购物车为空"),
    ORDER_NOT_MAKER(5005, "您不是该订单的制作者"),
    ORDER_CANNOT_CANCEL(5006, "该状态下无法取消订单"),
    BALANCE_INSUFFICIENT(5007, "余额不足，无法下单"),
    ORDER_CANNOT_REJECT(5008, "该状态下无法拒绝订单"),

    // 家庭相关 (7xxx)
    FAMILY_NOT_FOUND(7001, "未找到家庭"),
    FAMILY_ALREADY_JOINED(7002, "您已加入家庭"),
    FAMILY_INVITE_CODE_INVALID(7003, "邀请码无效"),
    FAMILY_NOT_ADMIN(7004, "您不是家庭管理员"),
    FAMILY_NOT_MEMBER(7005, "不是同一家庭成员"),

    // 文件相关 (6xxx)
    FILE_UPLOAD_ERROR(6001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORT(6002, "不支持的文件类型"),
    FILE_SIZE_EXCEED(6003, "文件大小超出限制");

    private final Integer code;
    private final String message;
}