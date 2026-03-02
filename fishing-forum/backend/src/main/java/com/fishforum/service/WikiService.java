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
 * 知识百科服务 - 词条CRUD、版本历史、搜索
 */
@Service
@RequiredArgsConstructor
public class WikiService {

    private final WikiEntryMapper entryMapper;
    private final WikiHistoryMapper historyMapper;
    private final UserMapper userMapper;

    // 获取词条列表（分页）
    public Result<?> listEntries(String category, String keyword, int page, int size) {
        Page<WikiEntry> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<WikiEntry> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty())
            wrapper.eq(WikiEntry::getCategory, category);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(WikiEntry::getTitle, keyword).or().like(WikiEntry::getContent, keyword));
        }
        wrapper.orderByDesc(WikiEntry::getUpdatedAt);
        Page<WikiEntry> result = entryMapper.selectPage(pageObj, wrapper);
        result.getRecords().forEach(this::enrichEntry);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return Result.ok(data);
    }

    // 获取词条详情
    public Result<?> getEntry(Long id) {
        WikiEntry entry = entryMapper.selectById(id);
        if (entry == null)
            return Result.error(404, "词条不存在");
        // 增加浏览量
        entry.setViewCount(entry.getViewCount() + 1);
        entryMapper.updateById(entry);
        enrichEntry(entry);
        return Result.ok(entry);
    }

    // 创建词条
    public Result<?> createEntry(WikiEntry entry, Long userId) {
        entry.setUserId(userId);
        entry.setVersion(1);
        entry.setViewCount(0);
        entryMapper.insert(entry);
        // 保存初始版本历史
        saveHistory(entry);
        return Result.ok("词条创建成功", entry);
    }

    // 编辑词条（生成新版本）
    public Result<?> updateEntry(Long id, WikiEntry update, Long userId) {
        WikiEntry entry = entryMapper.selectById(id);
        if (entry == null)
            return Result.error(404, "词条不存在");
        entry.setContent(update.getContent());
        if (update.getTitle() != null)
            entry.setTitle(update.getTitle());
        if (update.getCategory() != null)
            entry.setCategory(update.getCategory());
        entry.setVersion(entry.getVersion() + 1);
        entryMapper.updateById(entry);
        // 保存版本历史（记录编辑者）
        WikiHistory history = new WikiHistory();
        history.setEntryId(id);
        history.setContent(entry.getContent());
        history.setUserId(userId);
        history.setVersion(entry.getVersion());
        historyMapper.insert(history);
        return Result.ok("词条更新成功");
    }

    // 删除词条
    public Result<?> deleteEntry(Long id, Long userId, String role) {
        WikiEntry entry = entryMapper.selectById(id);
        if (entry == null)
            return Result.error(404, "词条不存在");
        if (!entry.getUserId().equals(userId) && !"ADMIN".equals(role))
            return Result.error(403, "无权删除");
        entryMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    // 获取版本历史
    public Result<?> getHistory(Long entryId) {
        List<WikiHistory> histories = historyMapper.selectList(
                new LambdaQueryWrapper<WikiHistory>()
                        .eq(WikiHistory::getEntryId, entryId)
                        .orderByDesc(WikiHistory::getVersion));
        histories.forEach(h -> {
            User u = userMapper.selectById(h.getUserId());
            if (u != null)
                h.setAuthorName(u.getUsername());
        });
        return Result.ok(histories);
    }

    // 获取分类列表
    public Result<?> getCategories() {
        List<String> categories = List.of("鱼种", "饵料", "装备", "技巧", "常识", "鱼种图鉴");
        return Result.ok(categories);
    }

    // 保存版本历史
    private void saveHistory(WikiEntry entry) {
        WikiHistory history = new WikiHistory();
        history.setEntryId(entry.getId());
        history.setContent(entry.getContent());
        history.setUserId(entry.getUserId());
        history.setVersion(entry.getVersion());
        historyMapper.insert(history);
    }

    private void enrichEntry(WikiEntry entry) {
        User u = userMapper.selectById(entry.getUserId());
        if (u != null)
            entry.setAuthorName(u.getUsername());
    }
}
