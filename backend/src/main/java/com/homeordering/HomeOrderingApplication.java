package com.homeordering;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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
        System.out.println("接口文档地址: http://localhost:8080/api");
        System.out.println("==================================");
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
