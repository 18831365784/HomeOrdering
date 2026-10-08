package com.homeordering.service.impl;

import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.service.FileUploadService;
import com.homeordering.util.FileUrlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
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
        ensureUploadDir(trueDir);

        String extension = resolveExtension(file);
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = dateStr + "_" + UUID.randomUUID().toString().replace("-", "") + extension;

        Path filePath = trueDir.toPath().resolve(fileName);
        try {
            Files.write(filePath, file.getBytes());
        } catch (FileSystemException e) {
            log.error("写入上传文件失败: {}", filePath, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), explainWriteFailure(e));
        }

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

    private void ensureUploadDir(File dir) {
        Path path = dir.toPath();
        try {
            if (Files.exists(path) && !Files.isDirectory(path)) {
                log.error("上传路径不是文件夹: {}", dir.getAbsolutePath());
                throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "上传路径不是文件夹");
            }
            Files.createDirectories(path);
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("创建上传目录失败: {}", dir.getAbsolutePath(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "创建上传目录失败");
        }
        if (!Files.isWritable(path)) {
            log.error("上传目录不可写: {}", dir.getAbsolutePath());
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "上传目录没有写入权限");
        }
    }

    private String explainWriteFailure(FileSystemException e) {
        if (e instanceof AccessDeniedException) {
            return "上传目录没有写入权限";
        }
        if (e instanceof NoSuchFileException || e instanceof NotDirectoryException) {
            return "上传目录不可用";
        }
        String reason = e.getReason() == null ? "" : e.getReason().toLowerCase();
        if (reason.contains("permission") || reason.contains("denied")) {
            return "上传目录没有写入权限";
        }
        if (reason.contains("space") || reason.contains("quota")) {
            return "服务器磁盘空间不足";
        }
        return "图片保存失败";
    }

    private File resolveUploadRoot() {
        String path = uploadPath == null ? "./uploads/" : uploadPath.trim();
        if (path.startsWith("./") || path.startsWith(".\\")) {
            return new File(System.getProperty("user.dir"), path.substring(2)).getAbsoluteFile();
        }
        return new File(path).getAbsoluteFile();
    }

    /** 微信 chooseAvatar 临时文件可能没有后缀，缺省按 png 存 */
    private String resolveExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf('.');
            if (dot >= 0 && dot < originalFilename.length() - 1) {
                String ext = originalFilename.substring(dot).toLowerCase();
                if (ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)")) {
                    return ext;
                }
            }
        }
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.contains("jpeg")) {
                return ".jpg";
            }
            if (contentType.contains("png")) {
                return ".png";
            }
            if (contentType.contains("gif")) {
                return ".gif";
            }
            if (contentType.contains("webp")) {
                return ".webp";
            }
        }
        return ".png";
    }
}
