package com.homeordering.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将相对路径转换为绝对路径
        String absoluteUploadPath = uploadPath;
        if (uploadPath.startsWith("./") || uploadPath.startsWith(".\\")) {
            // 获取项目根目录
            String projectPath = System.getProperty("user.dir");
            absoluteUploadPath = projectPath + File.separator + uploadPath.substring(2);
        }
        
        // 确保路径以文件协议和斜杠结尾
        if (!absoluteUploadPath.startsWith("file:")) {
            absoluteUploadPath = "file:" + absoluteUploadPath;
        }
        if (!absoluteUploadPath.endsWith("/") && !absoluteUploadPath.endsWith("\\")) {
            absoluteUploadPath += "/";
        }
        
        System.out.println("Upload path mapped to: " + absoluteUploadPath);
        
        // 映射图片访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absoluteUploadPath);
    }

    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}