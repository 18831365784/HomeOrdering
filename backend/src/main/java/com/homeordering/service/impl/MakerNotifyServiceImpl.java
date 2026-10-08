package com.homeordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeordering.config.WxSubscribeProperties;
import com.homeordering.entity.Order;
import com.homeordering.entity.OrderDetail;
import com.homeordering.entity.User;
import com.homeordering.mapper.OrderDetailMapper;
import com.homeordering.mapper.OrderMapper;
import com.homeordering.mapper.UserMapper;
import com.homeordering.service.MakerNotifyService;
import com.homeordering.util.SubscribeValue;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MakerNotifyServiceImpl implements MakerNotifyService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String TOKEN_URL =
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String SEND_URL =
            "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s";

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final UserMapper userMapper;
    private final WxSubscribeProperties subscribeProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${wx.appid}")
    private String appid;

    @Value("${wx.secret}")
    private String secret;

    private final Object tokenLock = new Object();
    private String accessToken;
    private long tokenExpireAt;

    private final ExecutorService notifier = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "maker-notify");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void notifyNewOrder(Long orderId) {
        if (orderId == null || !subscribeProperties.enabled()) {
            return;
        }
        notifier.execute(() -> {
            try {
                send(orderId, false);
            } catch (Exception e) {
                log.warn("制作提醒发送失败 orderId={} {}", orderId, e.getClass().getSimpleName());
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        notifier.shutdown();
    }

    private void send(Long orderId, boolean retried) throws Exception {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getMakerUuid() == null) {
            return;
        }
        if (order.getMakerUuid().equals(order.getCustomerUuid())) {
            return;
        }
        User maker = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUuid, order.getMakerUuid()));
        if (maker == null || maker.getOpenid() == null || maker.getOpenid().isBlank()) {
            log.warn("制作人没有 openid，跳过提醒 orderId={}", orderId);
            return;
        }
        String token = getAccessToken(false);
        if (token == null) {
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("touser", maker.getOpenid());
        payload.put("template_id", subscribeProperties.getMakerTemplateId().trim());
        payload.put("page", pageOf(orderId));
        payload.put("miniprogram_state", state());
        payload.put("lang", "zh_CN");
        payload.put("data", dataOf(order));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String response = restTemplate.postForObject(
                String.format(SEND_URL, token),
                new HttpEntity<>(objectMapper.writeValueAsString(payload), headers),
                String.class);
        if (response == null || response.isBlank()) {
            log.warn("制作提醒无响应 orderId={}", orderId);
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> body = objectMapper.readValue(response, Map.class);
        int errcode = errcode(body);
        if (errcode == 0) {
            log.info("已发送制作提醒 orderId={}", orderId);
            return;
        }
        if (!retried && (errcode == 40001 || errcode == 42001)) {
            synchronized (tokenLock) {
                accessToken = null;
                tokenExpireAt = 0;
            }
            send(orderId, true);
            return;
        }
        if (errcode == 43101) {
            log.info("制作人未订阅或提醒次数已用完 orderId={}", orderId);
            return;
        }
        log.warn("制作提醒被微信拒绝 orderId={} errcode={} errmsg={}", orderId, errcode, body.get("errmsg"));
    }

    private Map<String, Map<String, String>> dataOf(Order order) {
        List<OrderDetail> details = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, order.getId()));
        String dishes = "新订单";
        if (details != null && !details.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (OrderDetail detail : details) {
                if (builder.length() > 0) {
                    builder.append('、');
                }
                builder.append(detail.getDishName() == null ? "菜品" : detail.getDishName());
                builder.append('x');
                builder.append(detail.getQuantity() == null ? 1 : detail.getQuantity());
            }
            dishes = builder.toString();
        }
        String remark = order.getRemark() == null ? "" : order.getRemark().trim();
        Map<String, String> vars = new LinkedHashMap<>();
        vars.put("customer", order.getCustomerName() == null || order.getCustomerName().isBlank() ? "家人" : order.getCustomerName());
        vars.put("dishes", dishes);
        vars.put("time", order.getCreateTime() == null ? "" : order.getCreateTime().format(TIME_FORMAT));
        vars.put("remark", remark.isEmpty() ? "无" : remark);
        vars.put("orderNo", order.getOrderNo() == null ? "" : order.getOrderNo());

        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        subscribeProperties.getFields().forEach((keyword, template) -> {
            if (keyword == null || keyword.isBlank() || template == null) {
                return;
            }
            String rendered = template;
            for (Map.Entry<String, String> var : vars.entrySet()) {
                rendered = rendered.replace("{" + var.getKey() + "}", var.getValue());
            }
            data.put(keyword.trim(), Map.of("value", SubscribeValue.fit(keyword, rendered)));
        });
        return data;
    }

    private String pageOf(Long orderId) {
        String page = subscribeProperties.getPage();
        if (page == null || page.isBlank()) {
            return "pages/order/detail?id=" + orderId;
        }
        return page.replace("{orderId}", String.valueOf(orderId));
    }

    private String state() {
        String state = subscribeProperties.getMiniprogramState();
        if ("developer".equals(state) || "trial".equals(state) || "formal".equals(state)) {
            return state;
        }
        return "formal";
    }

    private String getAccessToken(boolean forceRefresh) throws Exception {
        synchronized (tokenLock) {
            long now = System.currentTimeMillis();
            if (!forceRefresh && accessToken != null && now < tokenExpireAt) {
                return accessToken;
            }
            if (appid == null || appid.isBlank() || secret == null || secret.isBlank()) {
                log.warn("未配置微信 AppId/Secret，跳过制作提醒");
                return null;
            }
            String response = restTemplate.getForObject(String.format(TOKEN_URL, appid, secret), String.class);
            if (response == null || response.isBlank()) {
                log.warn("获取微信 access_token 无响应");
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> body = objectMapper.readValue(response, Map.class);
            Object token = body.get("access_token");
            if (token == null) {
                log.warn("获取微信 access_token 失败 errcode={} errmsg={}", errcode(body), body.get("errmsg"));
                return null;
            }
            accessToken = String.valueOf(token);
            int expires = 7200;
            Object exp = body.get("expires_in");
            if (exp instanceof Number number) {
                expires = number.intValue();
            }
            tokenExpireAt = now + Math.max(60, expires - 300) * 1000L;
            return accessToken;
        }
    }

    private int errcode(Map<String, Object> body) {
        Object code = body.get("errcode");
        if (code instanceof Number number) {
            return number.intValue();
        }
        return -1;
    }
}
