package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.Result;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理服务 - 用户管理、内容审核、数据统计、公告管理
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ReportMapper reportMapper;
    private final AnnouncementMapper announcementMapper;
    private final FishingSpotMapper spotMapper;
    private final WikiEntryMapper wikiMapper;

    // ========== 数据统计 ==========
    public Result<?> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userMapper.selectCount(null));
        stats.put("postCount", postMapper.selectCount(null));
        stats.put("commentCount", commentMapper.selectCount(null));
        stats.put("spotCount", spotMapper.selectCount(null));
        stats.put("wikiCount", wikiMapper.selectCount(null));
        stats.put("pendingReports", reportMapper.selectCount(
                new LambdaQueryWrapper<Report>().eq(Report::getStatus, "PENDING")));
        return Result.ok(stats);
    }

    // ========== 用户管理 ==========
    public Result<?> listUsers(int page, int size, String keyword) {
        Page<User> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword).or().like(User::getEmail, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(pageObj, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return Result.ok(data);
    }

    // 修改用户角色
    public Result<?> changeUserRole(Long userId, String role) {
        User user = userMapper.selectById(userId);
        if (user == null)
            return Result.error("用户不存在");
        user.setRole(role);
        userMapper.updateById(user);
        return Result.ok("角色修改成功");
    }

    // 删除用户
    public Result<?> deleteUser(Long userId) {
        userMapper.deleteById(userId);
        return Result.ok("用户已删除");
    }

    // ========== 内容审核 ==========
    public Result<?> listReports(int page, int size, String status) {
        Page<Report> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty())
            wrapper.eq(Report::getStatus, status);
        wrapper.orderByDesc(Report::getCreatedAt);
        Page<Report> result = reportMapper.selectPage(pageObj, wrapper);
        result.getRecords().forEach(r -> {
            User u = userMapper.selectById(r.getReporterId());
            if (u != null)
                r.setReporterName(u.getUsername());
        });
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return Result.ok(data);
    }

    // 处理举报
    public Result<?> handleReport(Long id, String action) {
        Report report = reportMapper.selectById(id);
        if (report == null)
            return Result.error("举报不存在");
        if ("resolve".equals(action)) {
            report.setStatus("RESOLVED");
            // 删除被举报内容
            if ("POST".equals(report.getTargetType()))
                postMapper.deleteById(report.getTargetId());
            else if ("COMMENT".equals(report.getTargetType()))
                commentMapper.deleteById(report.getTargetId());
        } else {
            report.setStatus("REJECTED");
        }
        reportMapper.updateById(report);
        return Result.ok("处理成功");
    }

    // ========== 公告管理 ==========
    public Result<?> listAnnouncements() {
        List<Announcement> list = announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreatedAt));
        return Result.ok(list);
    }

    public Result<?> getActiveAnnouncements() {
        List<Announcement> list = announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>().eq(Announcement::getIsActive, true)
                        .orderByDesc(Announcement::getCreatedAt));
        return Result.ok(list);
    }

    public Result<?> createAnnouncement(Announcement announcement) {
        announcementMapper.insert(announcement);
        return Result.ok("公告发布成功");
    }

    public Result<?> updateAnnouncement(Long id, Announcement update) {
        Announcement ann = announcementMapper.selectById(id);
        if (ann == null)
            return Result.error("公告不存在");
        if (update.getTitle() != null)
            ann.setTitle(update.getTitle());
        if (update.getContent() != null)
            ann.setContent(update.getContent());
        if (update.getIsActive() != null)
            ann.setIsActive(update.getIsActive());
        announcementMapper.updateById(ann);
        return Result.ok("更新成功");
    }

    public Result<?> deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
        return Result.ok("删除成功");
    }
}
