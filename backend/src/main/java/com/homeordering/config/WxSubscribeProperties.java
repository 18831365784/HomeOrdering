package com.homeordering.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 制作提醒用的一次性订阅消息。模板 ID 留空则不下发。
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.subscribe")
public class WxSubscribeProperties {

    /** 公众平台选用的一次性订阅模板 ID */
    private String makerTemplateId = "";

    /** 点击通知打开的页面，可含 {orderId} */
    private String page = "pages/order/detail?id={orderId}";

    /** developer 开发版 / trial 体验版 / formal 正式版 */
    private String miniprogramState = "formal";

    /**
     * 键必须与模板关键词一致（如 thing1、time3）。
     * 值可含 {customer} {dishes} {time} {remark} {orderNo}。
     */
    private Map<String, String> fields = new LinkedHashMap<>();

    public boolean enabled() {
        return makerTemplateId != null && !makerTemplateId.isBlank()
                && fields != null && !fields.isEmpty();
    }
}
