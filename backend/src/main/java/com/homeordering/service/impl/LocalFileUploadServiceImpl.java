package com.homeordering.service.impl;

import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
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

@Service
public class LocalFileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${server.url}")
    private String serverUrl;

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        return uploadFile(file, null);
    }

    @Override
    public String uploadFile(MultipartFile file, String subDir) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "文件不能为空");
        }
        File trueDir = (subDir == null || subDir.isEmpty())
                ? new File(uploadPath)
                : new File(uploadPath, subDir);
        if (!trueDir.exists() && !trueDir.mkdirs()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "创建上传目录失败");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_SUPPORT);
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = dateStr + "_" + UUID.randomUUID().toString().replace("-", "") + extension;

        Path filePath = Paths.get(trueDir.getAbsolutePath(), fileName);
        Files.write(filePath, file.getBytes());

        String baseUrl = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        String urlTail = (subDir == null || subDir.isEmpty())
                ? ("/uploads/" + fileName)
                : ("/uploads/" + subDir + "/" + fileName);
        return baseUrl + contextPath + urlTail;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.contains("/uploads/")) {
                return false;
            }
            String relative = fileUrl.substring(fileUrl.indexOf("/uploads/") + "/uploads/".length());
            Path filePath = Paths.get(uploadPath, relative.split("/"));
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}
