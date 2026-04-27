package com.fishforum.controller;

import com.fishforum.common.FileTypeValidator;
import com.fishforum.common.Result;
import com.fishforum.common.UploadPathResolver;
import com.fishforum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * 认证与用户控制器
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    // ========== 公开接口（无需登录）==========

    // 注册
    @PostMapping("/api/auth/register")
    public Result<?> register(@RequestBody Map<String, String> body) {
        return userService.register(body.get("username"), body.get("password"), body.get("email"));
    }

    // 登录
    @PostMapping("/api/auth/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        return userService.login(body.get("username"), body.get("password"));
    }

    // 获取用户公开资料
    @GetMapping("/api/users/{id}/profile")
    public Result<?> getUserProfile(@PathVariable Long id) {
        return userService.getUserProfile(id);
    }

    // ========== 需要登录的接口 ==========

    // 获取当前用户信息
    @GetMapping("/api/users/me")
    public Result<?> getCurrentUser(Authentication auth) {
        return userService.getCurrentUser((Long) auth.getPrincipal());
    }

    // 当前用户成长信息
    @GetMapping("/api/users/me/growth")
    public Result<?> getGrowthProfile(Authentication auth) {
        return userService.getGrowthProfile((Long) auth.getPrincipal());
    }

    // 更新个人资料
    @PutMapping("/api/users/me")
    public Result<?> updateProfile(Authentication auth, @RequestBody com.fishforum.entity.User user) {
        return userService.updateProfile((Long) auth.getPrincipal(), user);
    }

    // 修改密码
    @PutMapping("/api/users/me/password")
    public Result<?> changePassword(Authentication auth, @RequestBody Map<String, String> body) {
        return userService.changePassword((Long) auth.getPrincipal(), body.get("oldPassword"), body.get("newPassword"));
    }

    // 上传头像
    @PostMapping("/api/users/me/avatar")
    public Result<?> uploadAvatar(Authentication auth, @RequestParam("file") MultipartFile file) throws IOException {
        Result<?> validation = FileTypeValidator.validateImage(file);
        if (validation.getCode() != 200) {
            return validation;
        }
        String fileName = UUID.randomUUID() + FileTypeValidator.safeImageExtension(file);
        // 使用绝对路径保存
        Path dir = UploadPathResolver.resolve(uploadPath).resolve("avatars");
        Files.createDirectories(dir);
        file.transferTo(dir.resolve(fileName).toFile());
        // 更新用户头像路径
        String avatarUrl = "/api/uploads/avatars/" + fileName;
        com.fishforum.entity.User u = new com.fishforum.entity.User();
        u.setAvatar(avatarUrl);
        return userService.updateProfile((Long) auth.getPrincipal(), u);
    }
}
