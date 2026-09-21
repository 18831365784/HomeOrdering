package com.homeordering;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 家庭点餐系统主启动类
 */
@SpringBootApplication
@MapperScan("com.homeordering.mapper")
public class HomeOrderingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeOrderingApplication.class, args);
        System.out.println("==================================");
        System.out.println("家庭点餐系统启动成功！");
        System.out.println("==================================");
    }
    
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(8000);
        return new RestTemplate(factory);
    }
}
