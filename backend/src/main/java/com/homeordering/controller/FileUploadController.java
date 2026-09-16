package com.homeordering.controller;

import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.common.Result;
import com.homeordering.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            return Result.success("文件上传成功", fileUploadService.uploadFile(file));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), e.getMessage());
        }
    }

    @PostMapping("/upload/icon")
    public Result<String> uploadCategoryIcon(@RequestParam("file") MultipartFile file) {
        try {
            return Result.success("文件上传成功", fileUploadService.uploadFile(file, "icon"));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public Result<String> deleteFile(@RequestParam String fileUrl) {
        boolean success = fileUploadService.deleteFile(fileUrl);
        if (!success) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "文件删除失败");
        }
        return Result.success("文件删除成功");
    }
}
