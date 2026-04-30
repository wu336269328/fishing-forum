package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.PageUtils;
import com.fishforum.common.Result;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import com.fishforum.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 社交消息服务 - 关注、私信、通知
 */
@Service
@RequiredArgsConstructor
public class SocialService {

    private final FollowMapper followMapper;
    private final MessageMapper messageMapper;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // ========== 关注功能 ==========

    // 关注/取消关注
    @Transactional
    public Result<?> toggleFollow(Long followingId, Long followerId) {
        if (followingId.equals(followerId))
            return Result.error("不能关注自己");
        if (userMapper.selectById(followingId) == null) {
            return Result.error(404, "用户不存在");
        }
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId).eq(Follow::getFollowingId, followingId);
        Follow existing = followMapper.selectOne(wrapper);

        if (existing != null) {
            followMapper.deleteById(existing.getId());
            return Result.ok("已取消关注");
        } else {
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFollowingId(followingId);
            followMapper.insert(follow);
            // 发送通知
            sendNotification(followingId, "FOLLOW", "新增关注", "有人关注了你", followerId);
            return Result.ok("关注成功");
        }
    }

    // 检查是否已关注
    public Result<?> checkFollow(Long followingId, Long followerId) {
        long count = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId).eq(Follow::getFollowingId, followingId));
        return Result.ok(count > 0);
    }

    // 获取关注列表
    public Result<?> getFollowings(Long userId) {
        List<Follow> follows = followMapper.selectList(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, userId));
        List<Long> userIds = follows.stream().map(Follow::getFollowingId).filter(Objects::nonNull).collect(Collectors.toList());
        List<User> users = userIds.isEmpty() ? List.of() : userMapper.selectBatchIds(userIds);
        return Result.ok(users.stream().map(UserVO::from).collect(Collectors.toList()));
    }

    // 获取粉丝列表
    public Result<?> getFollowers(Long userId) {
        List<Follow> follows = followMapper.selectList(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowingId, userId));
        List<Long> userIds = follows.stream().map(Follow::getFollowerId).filter(Objects::nonNull).collect(Collectors.toList());
        List<User> users = userIds.isEmpty() ? List.of() : userMapper.selectBatchIds(userIds);
        return Result.ok(users.stream().map(UserVO::from).collect(Collectors.toList()));
    }

    // 关注动态：当前用户关注的人最近发布的帖子
    public Result<?> getFollowFeed(Long userId, int page, int size) {
        List<Long> followingIds = followMapper.selectList(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, userId))
                .stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toList());
        if (followingIds.isEmpty()) {
            return Result.ok(Map.of("records", List.of(), "total", 0L, "pages", 0L));
        }
        Page<Post> pageObj = PageUtils.pageRequest(page, size);
        Page<Post> result = postMapper.selectPage(pageObj, new LambdaQueryWrapper<Post>()
                .in(Post::getUserId, followingIds)
                .orderByDesc(Post::getCreatedAt));
        fillPostAuthors(result.getRecords());
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        return Result.ok(data);
    }

    // ========== 私信功能 ==========

    // 发送私信
    @Transactional
    public Result<?> sendMessage(Long receiverId, String content, Long senderId) {
        if (receiverId.equals(senderId)) {
            return Result.error(400, "不能给自己发送私信");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "私信内容不能为空");
        }
        if (userMapper.selectById(receiverId) == null) {
            return Result.error(404, "接收者不存在");
        }
        if (content.length() > 2000) {
            return Result.error(400, "私信内容不能超过2000字");
        }
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content.trim());
        msg.setIsRead(false);
        messageMapper.insert(msg);
        // 通过WebSocket推送
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(), "/queue/messages", msg);
        sendNotification(receiverId, "MESSAGE", "收到新私信", "你收到了一条新私信", senderId);
        return Result.ok("发送成功", msg);
    }

    // 获取与某用户的对话记录
    public Result<?> getConversation(Long otherUserId, Long currentUserId, int page, int size) {
        if (userMapper.selectById(otherUserId) == null) {
            return Result.error(404, "用户不存在");
        }
        Page<Message> pageObj = PageUtils.pageRequest(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .and(w -> w
                        .and(w1 -> w1.eq(Message::getSenderId, currentUserId).eq(Message::getReceiverId, otherUserId))
                        .or(w2 -> w2.eq(Message::getSenderId, otherUserId).eq(Message::getReceiverId, currentUserId)))
                .orderByDesc(Message::getCreatedAt);
        Page<Message> result = messageMapper.selectPage(pageObj, wrapper);
        // 标记对方发来的消息为已读
        messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getSenderId, otherUserId).eq(Message::getReceiverId, currentUserId)
                .eq(Message::getIsRead, false))
                .forEach(m -> {
                    m.setIsRead(true);
                    messageMapper.updateById(m);
                });

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return Result.ok(data);
    }

    // 获取最近对话列表
    public Result<?> getRecentConversations(Long userId) {
        // 查询所有与当前用户相关的消息
        List<Message> msgs = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getSenderId, userId).or().eq(Message::getReceiverId, userId))
                .orderByDesc(Message::getCreatedAt));
        // 按对话对象分组，每组取最新一条
        Map<Long, Message> latestMap = new HashMap<>();
        for (Message m : msgs) {
            Long otherId = m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId();
            latestMap.putIfAbsent(otherId, m);
        }
        // 填充用户信息
        List<Map<String, Object>> conversations = latestMap.entrySet().stream().map(e -> {
            User u = userMapper.selectById(e.getKey());
            Map<String, Object> conv = new HashMap<>();
            conv.put("userId", e.getKey());
            conv.put("username", u != null ? u.getUsername() : "未知用户");
            conv.put("avatar", u != null ? u.getAvatar() : "");
            conv.put("lastMessage", e.getValue());
            // 统计未读数
            long unread = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                    .eq(Message::getSenderId, e.getKey()).eq(Message::getReceiverId, userId)
                    .eq(Message::getIsRead, false));
            conv.put("unreadCount", unread);
            return conv;
        }).collect(Collectors.toList());
        return Result.ok(conversations);
    }

    // ========== 通知功能 ==========

    // 获取用户通知
    public Result<?> getNotifications(Long userId, int page, int size) {
        Page<Notification> pageObj = PageUtils.pageRequest(page, size);
        Page<Notification> result = notificationMapper.selectPage(pageObj,
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreatedAt));
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return Result.ok(data);
    }

    // 获取未读通知数
    public Result<?> getUnreadCount(Long userId) {
        long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId).eq(Notification::getIsRead, false));
        long msgCount = messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, userId).eq(Message::getIsRead, false));
        Map<String, Long> data = new HashMap<>();
        data.put("notifications", count);
        data.put("messages", msgCount);
        data.put("total", count + msgCount);
        return Result.ok(data);
    }

    // 标记通知已读
    @Transactional
    public Result<?> markNotificationRead(Long id, Long userId) {
        Notification n = notificationMapper.selectById(id);
        if (n != null && n.getUserId().equals(userId)) {
            n.setIsRead(true);
            notificationMapper.updateById(n);
        }
        return Result.ok();
    }

    // 全部标记已读
    @Transactional
    public Result<?> markAllRead(Long userId) {
        notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId).eq(Notification::getIsRead, false))
                .forEach(n -> {
                    n.setIsRead(true);
                    notificationMapper.updateById(n);
                });
        return Result.ok("全部已读");
    }

    private void fillPostAuthors(List<Post> posts) {
        List<Long> userIds = posts.stream().map(Post::getUserId).filter(Objects::nonNull).distinct().toList();
        Map<Long, User> users = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        posts.forEach(post -> {
            User author = users.get(post.getUserId());
            if (author != null) {
                post.setAuthorName(author.getUsername());
                post.setAuthorAvatar(author.getAvatar());
            }
        });
    }

    // 发送通知（内部方法）
    public void sendNotification(Long userId, String type, String title, String content, Long relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        n.setIsRead(false);
        notificationMapper.insert(n);
        // WebSocket推送
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", n);
    }
}
