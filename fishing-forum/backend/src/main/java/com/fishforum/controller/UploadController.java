package com.fishforum.controller;

import com.fishforum.common.FileTypeValidator;
import com.fishforum.common.Result;
import com.fishforum.common.UploadPathResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 通用图片上传控制器
 */
@RestController
@RequiredArgsConstructor
public class UploadController {

    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    /**
     * 上传图片（用于帖子、百科、钓点等模块插入图片）
     */
    @PostMapping("/api/upload/image")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        Result<?> validation = FileTypeValidator.validateImage(file);
        if (validation.getCode() != 200) {
            return validation;
        }
        String fileName = UUID.randomUUID() + FileTypeValidator.safeImageExtension(file);
        // 使用绝对路径保存
        Path dir = UploadPathResolver.resolve(uploadPath).resolve("images");
        Files.createDirectories(dir);
        Path filePath = dir.resolve(fileName);
        file.transferTo(filePath.toFile());
        String url = "/api/uploads/images/" + fileName;
        return Result.ok("上传成功", url);
    }
}
