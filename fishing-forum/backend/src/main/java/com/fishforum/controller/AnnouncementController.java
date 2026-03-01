package com.fishforum.controller;

import com.fishforum.common.Result;
import com.fishforum.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公告公开接口（无需登录）
 */
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AdminService adminService;

    // 获取激活的公告
    @GetMapping
    public Result<?> getActiveAnnouncements() {
        return adminService.getActiveAnnouncements();
    }
}
