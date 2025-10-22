package com.homeordering.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {

    /**
     * 上传文件
     * @param file 文件
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file) throws Exception;

    /**
     * 上传文件到指定子目录
     * @param file 文件
     * @param subDir 子目录名，如"icon"（可为null/空代表主目录）
     * @return 访问URL
     */
    String uploadFile(MultipartFile file, String subDir) throws Exception;

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);
}
