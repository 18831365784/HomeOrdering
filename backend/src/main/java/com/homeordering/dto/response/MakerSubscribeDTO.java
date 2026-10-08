package com.homeordering.dto.response;

import lombok.Data;

@Data
public class MakerSubscribeDTO {

    private boolean enabled;

    /** 小程序 requestSubscribeMessage 使用的模板 ID */
    private String templateId;
}
