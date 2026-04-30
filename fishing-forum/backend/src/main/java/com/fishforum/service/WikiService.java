package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.PageUtils;
import com.fishforum.common.Result;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
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
        Page<WikiEntry> pageObj = PageUtils.pageRequest(page, size);
        LambdaQueryWrapper<WikiEntry> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty())
            wrapper.eq(WikiEntry::getCategory, category);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(WikiEntry::getTitle, keyword).or().like(WikiEntry::getContent, keyword));
        }
        wrapper.orderByDesc(WikiEntry::getUpdatedAt);
        Page<WikiEntry> result = entryMapper.selectPage(pageObj, wrapper);
        List<WikiEntry> records = result.getRecords();
        fillEntryAuthors(records);

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
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
        List<Long> commentIds = wikiCommentMapper.selectList(new LambdaQueryWrapper<WikiComment>().eq(WikiComment::getEntryId, id))
                .stream()
                .map(WikiComment::getId)
                .toList();
        for (Long commentId : commentIds) {
            likeMapper.deleteByTarget("WIKI_COMMENT", commentId);
            reportMapper.deleteByTarget("WIKI_COMMENT", commentId);
        }
        wikiCommentMapper.deleteByEntryId(id);
        entryMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    public Result<?> getComments(Long entryId) {
        List<WikiComment> all = wikiCommentMapper.selectList(
                new LambdaQueryWrapper<WikiComment>()
                        .eq(WikiComment::getEntryId, entryId)
                        .orderByAsc(WikiComment::getCreatedAt));
        fillCommentAuthors(all);
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
        if (parentId != null) {
            WikiComment parent = wikiCommentMapper.selectById(parentId);
            if (parent == null || !entryId.equals(parent.getEntryId())) {
                return Result.error(400, "父评论不存在或不属于该词条");
            }
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
        List<WikiComment> descendants = collectCommentDescendants(id);
        for (WikiComment child : descendants) {
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
        fillHistoryAuthors(histories);
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

    private void fillEntryAuthors(List<WikiEntry> entries) {
        Map<Long, User> users = usersById(entries.stream().map(WikiEntry::getUserId).collect(Collectors.toSet()));
        entries.forEach(entry -> {
            User user = users.get(entry.getUserId());
            if (user != null) {
                entry.setAuthorName(user.getUsername());
            }
        });
    }

    private void fillCommentAuthors(List<WikiComment> comments) {
        Map<Long, User> users = usersById(comments.stream().map(WikiComment::getUserId).collect(Collectors.toSet()));
        comments.forEach(comment -> {
            User user = users.get(comment.getUserId());
            if (user != null) {
                comment.setAuthorName(user.getUsername());
                comment.setAuthorAvatar(user.getAvatar());
            }
        });
    }

    private void fillHistoryAuthors(List<WikiHistory> histories) {
        Map<Long, User> users = usersById(histories.stream().map(WikiHistory::getUserId).collect(Collectors.toSet()));
        histories.forEach(history -> {
            User user = users.get(history.getUserId());
            if (user != null) {
                history.setAuthorName(user.getUsername());
            }
        });
    }

    private Map<Long, User> usersById(Set<Long> userIds) {
        Map<Long, User> users = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        for (Long userId : userIds) {
            if (!users.containsKey(userId)) {
                User user = userMapper.selectById(userId);
                if (user != null) {
                    users = new java.util.HashMap<>(users);
                    users.put(userId, user);
                }
            }
        }
        return users;
    }

    private List<WikiComment> collectCommentDescendants(Long rootId) {
        List<WikiComment> descendants = new ArrayList<>();
        Set<Long> visited = new java.util.HashSet<>();
        Queue<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        visited.add(rootId);
        while (!queue.isEmpty()) {
            Long parentId = queue.poll();
            List<WikiComment> children = wikiCommentMapper.selectList(
                    new LambdaQueryWrapper<WikiComment>().eq(WikiComment::getParentId, parentId));
            for (WikiComment child : children) {
                if (child.getId() != null && visited.add(child.getId())) {
                    descendants.add(child);
                    queue.add(child.getId());
                }
            }
        }
        return descendants;
    }
    private void enrichEntry(WikiEntry entry) {
        User u = userMapper.selectById(entry.getUserId());
        if (u != null)
            entry.setAuthorName(u.getUsername());
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
