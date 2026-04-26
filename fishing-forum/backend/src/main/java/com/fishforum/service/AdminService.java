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
import java.time.LocalDateTime;

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
    private final AdminLogMapper adminLogMapper;
    private final SensitiveWordMapper sensitiveWordMapper;

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
        stats.put("bannedUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getIsBanned, true)));
        stats.put("mutedUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>().gt(User::getMutedUntil, LocalDateTime.now())));
        stats.put("sensitiveWords", sensitiveWordMapper.selectCount(
                new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getIsActive, true)));
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
    public Result<?> changeUserRole(Long adminId, Long userId, String role) {
        if (!"USER".equals(role) && !"ADMIN".equals(role))
            return Result.error(400, "角色不合法");
        if (adminId.equals(userId) && "USER".equals(role))
            return Result.error(400, "不能降级当前管理员");
        User user = userMapper.selectById(userId);
        if (user == null)
            return Result.error("用户不存在");
        user.setRole(role);
        userMapper.updateById(user);
        log(adminId, "CHANGE_ROLE", "USER", userId, "role=" + role);
        return Result.ok("角色修改成功");
    }

    // 删除用户
    public Result<?> deleteUser(Long adminId, Long userId) {
        if (adminId.equals(userId))
            return Result.error(400, "不能删除当前管理员");
        userMapper.deleteById(userId);
        log(adminId, "DELETE_USER", "USER", userId, "delete user");
        return Result.ok("用户已删除");
    }

    public Result<?> setUserBanned(Long adminId, Long userId, boolean banned) {
        if (adminId.equals(userId))
            return Result.error(400, "不能封禁当前管理员");
        User user = userMapper.selectById(userId);
        if (user == null)
            return Result.error("用户不存在");
        user.setIsBanned(banned);
        userMapper.updateById(user);
        log(adminId, banned ? "BAN_USER" : "UNBAN_USER", "USER", userId, banned ? "ban user" : "unban user");
        return Result.ok(banned ? "用户已封禁" : "用户已解封");
    }

    public Result<?> setUserMuted(Long adminId, Long userId, boolean muted, Integer minutes) {
        if (adminId.equals(userId))
            return Result.error(400, "不能禁言当前管理员");
        User user = userMapper.selectById(userId);
        if (user == null)
            return Result.error("用户不存在");
        user.setMutedUntil(muted ? LocalDateTime.now().plusMinutes(minutes != null && minutes > 0 ? minutes : 60) : null);
        userMapper.updateById(user);
        log(adminId, muted ? "MUTE_USER" : "UNMUTE_USER", "USER", userId,
                muted ? "minutes=" + (minutes != null ? minutes : 60) : "unmute user");
        return Result.ok(muted ? "用户已禁言" : "用户已解除禁言");
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
    public Result<?> handleReport(Long adminId, Long id, String action, String reviewNote) {
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
        report.setReviewNote(reviewNote);
        report.setHandledBy(adminId);
        report.setHandledAt(LocalDateTime.now());
        reportMapper.updateById(report);
        log(adminId, "HANDLE_REPORT", report.getTargetType(), report.getTargetId(),
                action + (reviewNote != null ? ":" + reviewNote : ""));
        return Result.ok("处理成功");
    }

    public Result<?> listAdminLogs(int page, int size) {
        Page<AdminLog> pageObj = new Page<>(page, size);
        Page<AdminLog> result = adminLogMapper.selectPage(pageObj,
                new LambdaQueryWrapper<AdminLog>().orderByDesc(AdminLog::getCreatedAt));
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return Result.ok(data);
    }

    public Result<?> listSensitiveWords() {
        return Result.ok(sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>().orderByDesc(SensitiveWord::getCreatedAt)));
    }

    public Result<?> createSensitiveWord(Long adminId, String word) {
        if (word == null || word.trim().isEmpty())
            return Result.error(400, "敏感词不能为空");
        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(word.trim());
        sensitiveWord.setIsActive(true);
        sensitiveWord.setCreatedBy(adminId);
        sensitiveWordMapper.insert(sensitiveWord);
        log(adminId, "CREATE_SENSITIVE_WORD", "SENSITIVE_WORD", sensitiveWord.getId(), word.trim());
        return Result.ok("敏感词已添加");
    }

    public Result<?> setSensitiveWordActive(Long adminId, Long id, boolean active) {
        SensitiveWord word = sensitiveWordMapper.selectById(id);
        if (word == null)
            return Result.error("敏感词不存在");
        word.setIsActive(active);
        sensitiveWordMapper.updateById(word);
        log(adminId, active ? "ENABLE_SENSITIVE_WORD" : "DISABLE_SENSITIVE_WORD", "SENSITIVE_WORD", id, word.getWord());
        return Result.ok(active ? "敏感词已启用" : "敏感词已停用");
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

    private void log(Long adminId, String action, String targetType, Long targetId, String detail) {
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        adminLogMapper.insert(log);
    }
}
