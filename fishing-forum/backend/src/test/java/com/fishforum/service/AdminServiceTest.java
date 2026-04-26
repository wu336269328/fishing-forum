package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.Result;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock UserMapper userMapper;
    @Mock PostMapper postMapper;
    @Mock CommentMapper commentMapper;
    @Mock ReportMapper reportMapper;
    @Mock AnnouncementMapper announcementMapper;
    @Mock FishingSpotMapper spotMapper;
    @Mock WikiEntryMapper wikiMapper;
    @InjectMocks AdminService adminService;

    @Test
    void statisticsAggregatesAllDashboardCounters() {
        when(userMapper.selectCount(null)).thenReturn(1L);
        when(postMapper.selectCount(null)).thenReturn(2L);
        when(commentMapper.selectCount(null)).thenReturn(3L);
        when(spotMapper.selectCount(null)).thenReturn(4L);
        when(wikiMapper.selectCount(null)).thenReturn(5L);
        when(reportMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(6L);

        Map<?, ?> stats = (Map<?, ?>) adminService.getStatistics().getData();

        assertThat(stats.get("userCount")).isEqualTo(1L);
        assertThat(stats.get("postCount")).isEqualTo(2L);
        assertThat(stats.get("commentCount")).isEqualTo(3L);
        assertThat(stats.get("spotCount")).isEqualTo(4L);
        assertThat(stats.get("wikiCount")).isEqualTo(5L);
        assertThat(stats.get("pendingReports")).isEqualTo(6L);
    }

    @Test
    void listUsersSanitizesPasswords() {
        User user = new User();
        user.setPassword("secret");
        Page<User> page = new Page<>(1, 10);
        page.setRecords(List.of(user));
        page.setTotal(1);
        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Result<?> result = adminService.listUsers(1, 10, "fish");

        User returned = (User) ((List<?>) ((Map<?, ?>) result.getData()).get("records")).get(0);
        assertThat(returned.getPassword()).isNull();
    }

    @Test
    void changeUserRoleUpdatesExistingUser() {
        User user = new User();
        user.setId(3L);
        user.setRole("USER");
        when(userMapper.selectById(3L)).thenReturn(user);

        Result<?> result = adminService.changeUserRole(3L, "ADMIN");

        assertThat(result.getData()).isEqualTo("角色修改成功");
        assertThat(user.getRole()).isEqualTo("ADMIN");
        verify(userMapper).updateById(user);
    }

    @Test
    void handleReportResolvesAndDeletesReportedPostOrRejects() {
        Report postReport = report(9L, "POST");
        when(reportMapper.selectById(1L)).thenReturn(postReport);

        Result<?> resolved = adminService.handleReport(1L, "resolve");

        assertThat(resolved.getData()).isEqualTo("处理成功");
        assertThat(postReport.getStatus()).isEqualTo("RESOLVED");
        verify(postMapper).deleteById(9L);

        Report commentReport = report(10L, "COMMENT");
        when(reportMapper.selectById(2L)).thenReturn(commentReport);
        adminService.handleReport(2L, "reject");
        assertThat(commentReport.getStatus()).isEqualTo("REJECTED");
        verify(commentMapper, never()).deleteById(10L);
    }

    @Test
    void announcementCrudUsesMapper() {
        Announcement announcement = new Announcement();
        announcement.setId(4L);
        announcement.setTitle("old");
        announcement.setContent("old");
        announcement.setIsActive(true);
        when(announcementMapper.selectById(4L)).thenReturn(announcement);
        Announcement update = new Announcement();
        update.setTitle("new");
        update.setIsActive(false);

        adminService.createAnnouncement(new Announcement());
        Result<?> updated = adminService.updateAnnouncement(4L, update);
        Result<?> deleted = adminService.deleteAnnouncement(4L);

        assertThat(updated.getData()).isEqualTo("更新成功");
        assertThat(deleted.getData()).isEqualTo("删除成功");
        assertThat(announcement.getTitle()).isEqualTo("new");
        assertThat(announcement.getIsActive()).isFalse();
        verify(announcementMapper).insert(any(Announcement.class));
        verify(announcementMapper).updateById(announcement);
        verify(announcementMapper).deleteById(4L);
    }

    private static Report report(Long targetId, String targetType) {
        Report report = new Report();
        report.setTargetId(targetId);
        report.setTargetType(targetType);
        report.setStatus("PENDING");
        return report;
    }
}
