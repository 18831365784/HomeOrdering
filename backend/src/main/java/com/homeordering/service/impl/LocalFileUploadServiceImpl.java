package com.homeordering.service.impl;

import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.service.FileUploadService;
import com.homeordering.util.FileUrlHelper;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class LocalFileUploadServiceImpl implements FileUploadService {

    private final FileUrlHelper fileUrlHelper;

    @Value("${file.upload.path}")
    private String uploadPath;

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
                ? resolveUploadRoot()
                : new File(resolveUploadRoot(), subDir);
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

        String stored = (subDir == null || subDir.isEmpty())
                ? ("/uploads/" + fileName)
                : ("/uploads/" + subDir + "/" + fileName);
        // 接口立刻给可访问的绝对地址；业务入库时再 toStoredPath 剥主机
        return fileUrlHelper.toPublicUrl(stored);
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            String stored = fileUrlHelper.toStoredPath(fileUrl);
            if (stored == null || !stored.startsWith("/uploads/")) {
                return false;
            }
            String relative = stored.substring("/uploads/".length());
            Path filePath = Paths.get(resolveUploadRoot().getAbsolutePath(), relative.split("/"));
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    private File resolveUploadRoot() {
        String path = uploadPath == null ? "./uploads/" : uploadPath.trim();
        if (path.startsWith("./") || path.startsWith(".\\")) {
            return new File(System.getProperty("user.dir"), path.substring(2)).getAbsoluteFile();
        }
        return new File(path).getAbsoluteFile();
    }
}
