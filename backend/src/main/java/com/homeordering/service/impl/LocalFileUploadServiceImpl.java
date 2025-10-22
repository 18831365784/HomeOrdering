package com.homeordering.service.impl;

import com.homeordering.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件上传服务实现类
 * 可扩展为云存储服务
 */
@Service
public class LocalFileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.type:local}")
    private String uploadType;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    // 注入服务器地址，支持配置文件中设置完整URL
    @Value("${server.url}")
    private String serverUrl;

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // TODO: 如果是云存储类型，可以在这里切换到云存储实现
        if ("cloud".equals(uploadType)) {
            return uploadToCloud(file);
        } else {
            return uploadToLocal(file);
        }
    }

    /**
     * 上传到指定子目录
     */
    @Override
    public String uploadFile(MultipartFile file, String subDir) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        // 处理路径（子目录可为null/空）
        File trueDir = (subDir == null || subDir.isEmpty()) ? new File(uploadPath) : new File(uploadPath, subDir);
        if (!trueDir.exists()) {
            trueDir.mkdirs();
        }
        // 构造文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = dateStr + "_" + UUID.randomUUID().toString().replace("-", "") + extension;
        // 写入
        Path filePath = Paths.get(trueDir.getAbsolutePath(), fileName);
        Files.write(filePath, file.getBytes());
        // 生成url  /uploads/icon/xxx.png (或不带icon)
        String baseUrl = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        String urlTail = (subDir == null || subDir.isEmpty()) ? ("/uploads/" + fileName) : ("/uploads/" + subDir + "/" + fileName);
        return baseUrl + contextPath + urlTail;
    }

    /**
     * 上传到本地
     */
    private String uploadToLocal(MultipartFile file) throws IOException {
        // 确保上传目录存在
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = dateStr + "_" + UUID.randomUUID().toString().replace("-", "") + extension;

        // 保存文件
        Path filePath = Paths.get(uploadPath, fileName);
        Files.write(filePath, file.getBytes());

        // 返回完整URL路径，供前端通过HTTP访问
        // 移除serverUrl末尾可能存在的斜杠，确保URL格式正确
        String baseUrl = serverUrl;
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + contextPath + "/uploads/" + fileName;
    }

    /**
     * 上传到云存储（预留接口，可扩展）
     * 示例：阿里云OSS、腾讯云COS等
     */
    private String uploadToCloud(MultipartFile file) throws Exception {
        // TODO: 实现云存储上传逻辑
        // 1. 获取云存储配置（accessKey、secretKey、bucket等）
        // 2. 初始化云存储客户端
        // 3. 上传文件
        // 4. 返回云存储URL
        
        throw new UnsupportedOperationException("云存储功能待实现，请在配置文件中配置云存储参数");
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl.contains("/uploads/")) {
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadPath, fileName);
                return Files.deleteIfExists(filePath);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}