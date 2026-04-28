package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.Result;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识百科服务 - 词条CRUD、版本历史、搜索
 */
@Service
@RequiredArgsConstructor
public class WikiService {

    private final WikiEntryMapper entryMapper;
    private final WikiHistoryMapper historyMapper;
    private final WikiCommentMapper wikiCommentMapper;
    private final UserMapper userMapper;
    private final LikeMapper likeMapper;
    private final ReportMapper reportMapper;
    private final SocialService socialService;

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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
    public Result<?> deleteEntry(Long id, Long userId, String role) {
        WikiEntry entry = entryMapper.selectById(id);
        if (entry == null)
            return Result.error(404, "词条不存在");
        if (!entry.getUserId().equals(userId) && !"ADMIN".equals(role))
            return Result.error(403, "无权删除");
        wikiCommentMapper.deleteByEntryId(id);
        entryMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    public Result<?> getComments(Long entryId) {
        List<WikiComment> all = wikiCommentMapper.selectList(
                new LambdaQueryWrapper<WikiComment>()
                        .eq(WikiComment::getEntryId, entryId)
                        .orderByAsc(WikiComment::getCreatedAt));
        all.forEach(this::enrichComment);
        return Result.ok(buildCommentTree(all));
    }

    @Transactional
    public Result<?> addComment(Long entryId, String content, Long parentId, Long userId) {
        WikiEntry entry = entryMapper.selectById(entryId);
        if (entry == null) {
            return Result.error(404, "词条不存在");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "评论内容不能为空");
        }
        if (content.length() > 2000) {
            return Result.error(400, "评论内容不能超过2000字");
        }
        WikiComment comment = new WikiComment();
        comment.setEntryId(entryId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(content.trim());
        comment.setLikeCount(0);
        wikiCommentMapper.insert(comment);

        if (parentId != null) {
            WikiComment parent = wikiCommentMapper.selectById(parentId);
            if (parent != null && !parent.getUserId().equals(userId)) {
                socialService.sendNotification(parent.getUserId(), "WIKI_COMMENT_REPLY", "百科评论有新回复",
                        "你的百科评论收到了回复", entryId);
            }
        } else if (!entry.getUserId().equals(userId)) {
            socialService.sendNotification(entry.getUserId(), "WIKI_COMMENT", "百科词条有新讨论",
                    "你的百科词条收到了新评论", entryId);
        }
        return Result.ok("评论成功", comment);
    }

    @Transactional
    public Result<?> deleteComment(Long id, Long userId, String role) {
        WikiComment comment = wikiCommentMapper.selectById(id);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        if (!comment.getUserId().equals(userId) && !"ADMIN".equals(role)) {
            return Result.error(403, "无权删除");
        }
        List<WikiComment> children = wikiCommentMapper.selectList(
                new LambdaQueryWrapper<WikiComment>().eq(WikiComment::getParentId, id));
        for (WikiComment child : children) {
            likeMapper.deleteByTarget("WIKI_COMMENT", child.getId());
            reportMapper.deleteByTarget("WIKI_COMMENT", child.getId());
            wikiCommentMapper.deleteById(child.getId());
        }
        likeMapper.deleteByTarget("WIKI_COMMENT", id);
        reportMapper.deleteByTarget("WIKI_COMMENT", id);
        wikiCommentMapper.deleteById(id);
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

    private void enrichComment(WikiComment comment) {
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            comment.setAuthorName(user.getUsername());
            comment.setAuthorAvatar(user.getAvatar());
        }
    }

    private List<WikiComment> buildCommentTree(List<WikiComment> all) {
        Map<Long, WikiComment> byId = all.stream().collect(Collectors.toMap(WikiComment::getId, c -> c));
        List<WikiComment> roots = new ArrayList<>();
        for (WikiComment comment : all) {
            comment.setChildren(new ArrayList<>());
            if (comment.getParentId() == null) {
                roots.add(comment);
                continue;
            }
            WikiComment parent = byId.get(comment.getParentId());
            if (parent != null) {
                parent.getChildren().add(comment);
            }
        }
        return roots;
    }
}
