package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fishforum.common.Result;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 评论与互动服务 - 评论、点赞、收藏、举报
 */
@Service
@RequiredArgsConstructor
public class InteractionService {

    private final CommentMapper commentMapper;
    private final LikeMapper likeMapper;
    private final FavoriteMapper favoriteMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final ReportMapper reportMapper;
    private final WikiCommentMapper wikiCommentMapper;
    private final SocialService socialService;

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\p{IsHan}\\w-]{2,20})");

    // 获取帖子评论（树形结构）
    public Result<?> getComments(Long postId) {
        List<Comment> all = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, postId).orderByAsc(Comment::getCreatedAt));
        // 填充作者信息
        all.forEach(c -> {
            User u = userMapper.selectById(c.getUserId());
            if (u != null) {
                c.setAuthorName(u.getUsername());
                c.setAuthorAvatar(u.getAvatar());
            }
        });
        // 构建树形结构
        List<Comment> tree = buildCommentTree(all);
        return Result.ok(tree);
    }

    // 发表评论
    @Transactional
    public Result<?> addComment(Long postId, String content, Long parentId, Long userId) {
        User currentUser = userMapper.selectById(userId);
        if (currentUser != null && currentUser.getMutedUntil() != null
                && currentUser.getMutedUntil().isAfter(java.time.LocalDateTime.now()))
            return Result.error(403, "账号已被禁言，暂不能评论");
        if (content == null || content.trim().isEmpty())
            return Result.error(400, "评论内容不能为空");
        if (content.length() > 2000)
            return Result.error(400, "评论内容不能超过2000字");
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setUserId(userId);
        comment.setLikeCount(0);
        commentMapper.insert(comment);
        // 更新帖子评论数
        Post post = postMapper.selectById(postId);
        if (post != null) {
            postMapper.incrementCommentCount(postId, 1);
            notifyCommentParticipants(post, comment, content, userId);
        }
        return Result.ok("评论成功", comment);
    }

    // 删除评论
    @Transactional
    public Result<?> deleteComment(Long id, Long userId, String role) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null)
            return Result.error(404, "评论不存在");
        if (!comment.getUserId().equals(userId) && !"ADMIN".equals(role)) {
            return Result.error(403, "无权删除");
        }
        // 级联删除子评论
        List<Comment> children = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, id));
        int deletedCount = 1 + children.size();
        for (Comment child : children) {
            commentMapper.deleteById(child.getId());
            likeMapper.deleteByTarget("COMMENT", child.getId());
            reportMapper.deleteByTarget("COMMENT", child.getId());
        }
        likeMapper.deleteByTarget("COMMENT", id);
        reportMapper.deleteByTarget("COMMENT", id);
        commentMapper.deleteById(id);
        postMapper.incrementCommentCount(comment.getPostId(), -deletedCount);
        return Result.ok("删除成功");
    }

    // 点赞/取消点赞
    @Transactional
    public Result<?> toggleLike(Long targetId, String targetType, Long userId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<Like>()
                .eq(Like::getUserId, userId).eq(Like::getTargetId, targetId).eq(Like::getTargetType, targetType);
        Like existing = likeMapper.selectOne(wrapper);

        if (existing != null) {
            // 取消点赞
            likeMapper.deleteById(existing.getId());
            updateLikeCount(targetId, targetType, -1);
            return Result.ok("已取消点赞");
        } else {
            // 点赞
            Like like = new Like();
            like.setUserId(userId);
            like.setTargetId(targetId);
            like.setTargetType(targetType);
            likeMapper.insert(like);
            updateLikeCount(targetId, targetType, 1);
            notifyLike(targetId, targetType, userId);
            return Result.ok("点赞成功");
        }
    }

    private void notifyCommentParticipants(Post post, Comment comment, String content, Long actorId) {
        if (comment.getParentId() != null) {
            Comment parent = commentMapper.selectById(comment.getParentId());
            if (parent != null && !parent.getUserId().equals(actorId)) {
                socialService.sendNotification(parent.getUserId(), "COMMENT_REPLY", "收到回复",
                        "你的评论有了新回复", post.getId());
            }
        } else if (!post.getUserId().equals(actorId)) {
            socialService.sendNotification(post.getUserId(), "COMMENT", "帖子有新评论",
                    "你的帖子收到了新评论", post.getId());
        }
        notifyMentions(content, actorId, post.getId());
    }

    private void notifyMentions(String content, Long actorId, Long postId) {
        if (content == null || !content.contains("@")) {
            return;
        }
        Set<String> names = new HashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        for (String name : names) {
            User mentioned = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, name));
            if (mentioned != null && !mentioned.getId().equals(actorId)) {
                socialService.sendNotification(mentioned.getId(), "MENTION", "有人提到了你",
                        "评论中提到了 @" + name, postId);
            }
        }
    }

    private void notifyLike(Long targetId, String targetType, Long actorId) {
        if ("POST".equals(targetType)) {
            Post post = postMapper.selectById(targetId);
            if (post != null && !post.getUserId().equals(actorId)) {
                socialService.sendNotification(post.getUserId(), "LIKE", "帖子被点赞",
                        "你的帖子收到了新的点赞", targetId);
            }
        } else if ("COMMENT".equals(targetType)) {
            Comment comment = commentMapper.selectById(targetId);
            if (comment != null && !comment.getUserId().equals(actorId)) {
                socialService.sendNotification(comment.getUserId(), "LIKE", "评论被点赞",
                        "你的评论收到了新的点赞", comment.getPostId());
            }
        } else if ("WIKI_COMMENT".equals(targetType)) {
            WikiComment comment = wikiCommentMapper.selectById(targetId);
            if (comment != null && !comment.getUserId().equals(actorId)) {
                socialService.sendNotification(comment.getUserId(), "LIKE", "百科评论被点赞",
                        "你的百科评论收到了新的点赞", comment.getEntryId());
            }
        }
    }

    // 收藏/取消收藏
    @Transactional
    public Result<?> toggleFavorite(Long postId, Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getPostId, postId);
        Favorite existing = favoriteMapper.selectOne(wrapper);

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return Result.ok("已取消收藏");
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setPostId(postId);
            favoriteMapper.insert(fav);
            Post post = postMapper.selectById(postId);
            if (post != null && !post.getUserId().equals(userId)) {
                socialService.sendNotification(post.getUserId(), "FAVORITE", "帖子被收藏",
                        "你的帖子被用户收藏", postId);
            }
            return Result.ok("收藏成功");
        }
    }

    // 获取用户收藏列表
    public Result<?> getUserFavorites(Long userId) {
        List<Favorite> favs = favoriteMapper.selectList(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreatedAt));
        List<Post> posts = favs.stream()
                .map(f -> postMapper.selectById(f.getPostId()))
                .filter(p -> p != null)
                .collect(Collectors.toList());
        return Result.ok(posts);
    }

    // 举报
    @Transactional
    public Result<?> report(Long targetId, String targetType, String reason, Long userId) {
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error(400, "举报原因不能为空");
        }
        if (!Set.of("POST", "COMMENT", "USER", "WIKI_COMMENT").contains(targetType)) {
            return Result.error(400, "不支持的举报类型");
        }
        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetId(targetId);
        report.setTargetType(targetType);
        report.setReason(reason.trim());
        report.setStatus("PENDING");
        reportMapper.insert(report);
        return Result.ok("举报成功，管理员将尽快处理");
    }

    // 更新点赞计数
    private void updateLikeCount(Long targetId, String type, int delta) {
        if ("POST".equals(type)) {
            Post post = postMapper.selectById(targetId);
            if (post != null) {
                postMapper.incrementLikeCount(targetId, delta);
            }
        } else if ("COMMENT".equals(type)) {
            Comment c = commentMapper.selectById(targetId);
            if (c != null) {
                commentMapper.incrementLikeCount(targetId, delta);
            }
        } else if ("WIKI_COMMENT".equals(type)) {
            WikiComment c = wikiCommentMapper.selectById(targetId);
            if (c != null) {
                wikiCommentMapper.incrementLikeCount(targetId, delta);
            }
        }
    }

    // 构建评论树
    private List<Comment> buildCommentTree(List<Comment> all) {
        Map<Long, Comment> map = all.stream().collect(Collectors.toMap(Comment::getId, c -> c));
        List<Comment> roots = new ArrayList<>();
        for (Comment c : all) {
            c.setChildren(new ArrayList<>());
            if (c.getParentId() == null) {
                roots.add(c);
            } else {
                Comment parent = map.get(c.getParentId());
                if (parent != null)
                    parent.getChildren().add(c);
            }
        }
        return roots;
    }
}
