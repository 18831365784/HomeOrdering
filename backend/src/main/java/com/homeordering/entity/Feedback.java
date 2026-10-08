package com.homeordering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问题反馈。提交人身份只从登录态写入。
 */
@Data
@TableName("feedback")
public class Feedback implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userUuid;

    private String nickname;

    private Long familyId;

    private String content;

    private LocalDateTime createTime;
}
