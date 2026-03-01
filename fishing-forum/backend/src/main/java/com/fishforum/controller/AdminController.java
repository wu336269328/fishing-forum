package com.fishforum.controller;

import com.fishforum.common.Result;
import com.fishforum.entity.Announcement;
import com.fishforum.service.AdminService;
import com.fishforum.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台管理控制器（仅管理员可访问）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final PostService postService;

    // 数据统计仪表盘
    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        return adminService.getStatistics();
    }

    // ========== 用户管理 ==========

    @GetMapping("/users")
    public Result<?> listUsers(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return adminService.listUsers(page, size, keyword);
    }

    @PutMapping("/users/{id}/role")
    public Result<?> changeRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return adminService.changeUserRole(id, body.get("role"));
    }

    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        return adminService.deleteUser(id);
    }

    // ========== 内容审核 ==========

    @GetMapping("/reports")
    public Result<?> listReports(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return adminService.listReports(page, size, status);
    }

    @PutMapping("/reports/{id}")
    public Result<?> handleReport(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return adminService.handleReport(id, body.get("action"));
    }

    // ========== 帖子管理 ==========

    // 置顶/取消置顶
    @PutMapping("/posts/{id}/top")
    public Result<?> toggleTop(@PathVariable Long id) {
        return postService.toggleTop(id);
    }

    // 精华/取消精华
    @PutMapping("/posts/{id}/featured")
    public Result<?> toggleFeatured(@PathVariable Long id) {
        return postService.toggleFeatured(id);
    }

    // ========== 公告管理 ==========

    @GetMapping("/announcements")
    public Result<?> listAnnouncements() {
        return adminService.listAnnouncements();
    }

    @PostMapping("/announcements")
    public Result<?> createAnnouncement(@RequestBody Announcement announcement) {
        return adminService.createAnnouncement(announcement);
    }

    @PutMapping("/announcements/{id}")
    public Result<?> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        return adminService.updateAnnouncement(id, announcement);
    }

    @DeleteMapping("/announcements/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        return adminService.deleteAnnouncement(id);
    }
}
