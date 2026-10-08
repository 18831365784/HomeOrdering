package com.homeordering.dto.request;

import lombok.Data;

@Data
public class FeedbackRequest {
    /** 问题描述 */
    private String content;
}
