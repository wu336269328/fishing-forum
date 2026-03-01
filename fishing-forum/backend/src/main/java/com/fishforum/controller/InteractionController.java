package com.fishforum.controller;

import com.fishforum.common.Result;
import com.fishforum.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论与互动控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    // 获取帖子评论（公开）
    @GetMapping("/comments/{postId}")
    public Result<?> getComments(@PathVariable Long postId) {
        return interactionService.getComments(postId);
    }

    // 发表评论
    @PostMapping("/comments")
    public Result<?> addComment(@RequestBody Map<String, Object> body, Authentication auth) {
        Long postId = Long.valueOf(body.get("postId").toString());
        String content = (String) body.get("content");
        Long parentId = body.get("parentId") != null ? Long.valueOf(body.get("parentId").toString()) : null;
        return interactionService.addComment(postId, content, parentId, (Long) auth.getPrincipal());
    }

    // 删除评论
    @DeleteMapping("/comments/{id}")
    public Result<?> deleteComment(@PathVariable Long id, Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return interactionService.deleteComment(id, (Long) auth.getPrincipal(), role);
    }

    // 点赞/取消点赞
    @PostMapping("/likes")
    public Result<?> toggleLike(@RequestBody Map<String, Object> body, Authentication auth) {
        Long targetId = Long.valueOf(body.get("targetId").toString());
        String targetType = (String) body.get("targetType");
        return interactionService.toggleLike(targetId, targetType, (Long) auth.getPrincipal());
    }

    // 收藏/取消收藏
    @PostMapping("/favorites")
    public Result<?> toggleFavorite(@RequestBody Map<String, Long> body, Authentication auth) {
        return interactionService.toggleFavorite(body.get("postId"), (Long) auth.getPrincipal());
    }

    // 获取用户收藏
    @GetMapping("/favorites")
    public Result<?> getUserFavorites(Authentication auth) {
        return interactionService.getUserFavorites((Long) auth.getPrincipal());
    }

    // 举报
    @PostMapping("/reports")
    public Result<?> report(@RequestBody Map<String, Object> body, Authentication auth) {
        Long targetId = Long.valueOf(body.get("targetId").toString());
        String targetType = (String) body.get("targetType");
        String reason = (String) body.get("reason");
        return interactionService.report(targetId, targetType, reason, (Long) auth.getPrincipal());
    }
}
