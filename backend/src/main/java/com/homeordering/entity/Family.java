package com.homeordering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 家庭实体类
 */
@Data
@TableName("family")
public class Family implements Serializable {

    /**
     * 主键ID - 使用雪花算法随机生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 家庭名称
     */
    private String name;

    /**
     * 邀请码（6位）
     */
    private String inviteCode;

    /**
     * 管理员UUID
     */
    private String adminUuid;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
