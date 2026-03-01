package com.fishforum.controller;

import com.fishforum.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
        if (file.isEmpty()) {
            return Result.error("请选择图片");
        }
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只允许上传图片文件");
        }
        // 限制大小 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("图片大小不超过10MB");
        }
        // 生成唯一文件名
        String ext = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + ext;
        // 使用绝对路径保存
        Path dir = Paths.get(uploadPath, "images").toAbsolutePath();
        Files.createDirectories(dir);
        Path filePath = dir.resolve(fileName);
        file.transferTo(filePath.toFile());
        String url = "/api/uploads/images/" + fileName;
        return Result.ok("上传成功", url);
    }
}
