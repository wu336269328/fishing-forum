package com.fishforum.controller;

import com.fishforum.common.Result;
import com.fishforum.service.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 社交与消息控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;

    // ========== 关注 ==========

    // 关注/取消关注
    @PostMapping("/follows/{followingId}")
    public Result<?> toggleFollow(@PathVariable Long followingId, Authentication auth) {
        return socialService.toggleFollow(followingId, (Long) auth.getPrincipal());
    }

    // 检查是否已关注
    @GetMapping("/follows/check/{followingId}")
    public Result<?> checkFollow(@PathVariable Long followingId, Authentication auth) {
        if (auth == null)
            return Result.ok(false);
        return socialService.checkFollow(followingId, (Long) auth.getPrincipal());
    }

    // 获取关注列表
    @GetMapping("/users/{userId}/followings")
    public Result<?> getFollowings(@PathVariable Long userId) {
        return socialService.getFollowings(userId);
    }

    // 获取粉丝列表
    @GetMapping("/users/{userId}/followers")
    public Result<?> getFollowers(@PathVariable Long userId) {
        return socialService.getFollowers(userId);
    }

    // ========== 私信 ==========

    // 发送私信
    @PostMapping("/messages")
    public Result<?> sendMessage(@RequestBody Map<String, Object> body, Authentication auth) {
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String content = (String) body.get("content");
        return socialService.sendMessage(receiverId, content, (Long) auth.getPrincipal());
    }

    // 获取与某用户的对话
    @GetMapping("/messages/{otherUserId}")
    public Result<?> getConversation(@PathVariable Long otherUserId, Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return socialService.getConversation(otherUserId, (Long) auth.getPrincipal(), page, size);
    }

    // 获取最近对话列表
    @GetMapping("/messages")
    public Result<?> getRecentConversations(Authentication auth) {
        if (auth == null)
            return Result.error(401, "请先登录");
        return socialService.getRecentConversations((Long) auth.getPrincipal());
    }

    // ========== 通知 ==========

    // 获取通知列表
    @GetMapping("/notifications")
    public Result<?> getNotifications(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (auth == null)
            return Result.error(401, "请先登录");
        return socialService.getNotifications((Long) auth.getPrincipal(), page, size);
    }

    // 获取未读数
    @GetMapping("/notifications/unread-count")
    public Result<?> getUnreadCount(Authentication auth) {
        if (auth == null)
            return Result.ok(java.util.Map.of("total", 0, "messages", 0, "notifications", 0));
        return socialService.getUnreadCount((Long) auth.getPrincipal());
    }

    // 标记通知已读
    @PutMapping("/notifications/{id}/read")
    public Result<?> markRead(@PathVariable Long id, Authentication auth) {
        return socialService.markNotificationRead(id, (Long) auth.getPrincipal());
    }

    // 全部标记已读
    @PutMapping("/notifications/read-all")
    public Result<?> markAllRead(Authentication auth) {
        return socialService.markAllRead((Long) auth.getPrincipal());
    }
}
