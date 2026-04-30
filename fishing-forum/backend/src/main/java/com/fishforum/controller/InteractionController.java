package com.fishforum.controller;

import com.fishforum.common.Result;
import com.fishforum.dto.CommentCreateRequest;
import com.fishforum.dto.FavoriteRequest;
import com.fishforum.dto.LikeRequest;
import com.fishforum.dto.ReportCreateRequest;
import com.fishforum.service.InteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public Result<?> addComment(@Valid @RequestBody CommentCreateRequest request, Authentication auth) {
        return interactionService.addComment(request.getPostId(), request.getContent(), request.getParentId(),
                (Long) auth.getPrincipal());
    }

    // 删除评论
    @DeleteMapping("/comments/{id}")
    public Result<?> deleteComment(@PathVariable Long id, Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return interactionService.deleteComment(id, (Long) auth.getPrincipal(), role);
    }

    // 点赞/取消点赞
    @PostMapping("/likes")
    public Result<?> toggleLike(@Valid @RequestBody LikeRequest request, Authentication auth) {
        return interactionService.toggleLike(request.getTargetId(), request.getTargetType(), (Long) auth.getPrincipal());
    }

    // 收藏/取消收藏
    @PostMapping("/favorites")
    public Result<?> toggleFavorite(@Valid @RequestBody FavoriteRequest request, Authentication auth) {
        return interactionService.toggleFavorite(request.getPostId(), (Long) auth.getPrincipal());
    }

    // 获取用户收藏
    @GetMapping("/favorites")
    public Result<?> getUserFavorites(Authentication auth) {
        if (auth == null)
            return Result.error(401, "请先登录");
        return interactionService.getUserFavorites((Long) auth.getPrincipal());
    }

    // 举报
    @PostMapping("/reports")
    public Result<?> report(@Valid @RequestBody ReportCreateRequest request, Authentication auth) {
        return interactionService.report(request.getTargetId(), request.getTargetType(), request.getReason(),
                (Long) auth.getPrincipal());
    }
}
