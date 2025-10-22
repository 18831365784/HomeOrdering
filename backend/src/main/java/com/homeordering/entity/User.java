package com.homeordering.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 用户唯一标识
     */
    private String uuid;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 角色: 0-普通用户 1-管理员
     */
    private Integer role;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
