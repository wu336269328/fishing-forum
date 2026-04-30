package com.fishforum.controller;

import com.fishforum.common.Result;
import com.fishforum.dto.PostCreateRequest;
import com.fishforum.entity.Post;
import com.fishforum.service.AdminService;
import com.fishforum.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子与板块控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AdminService adminService;

    // 公开统计接口（首页用，无需登录）
    @GetMapping("/statistics/public")
    public Result<?> getPublicStats() {
        return adminService.getStatistics();
    }

    // 获取帖子列表（公开）
    @GetMapping("/posts")
    public Result<?> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String postType,
            @RequestParam(required = false) Long tagId) {
        return postService.listPosts(page, size, sectionId, keyword, sort, postType, tagId);
    }

    // 获取帖子详情（公开）
    @GetMapping("/posts/{id}")
    public Result<?> getPost(@PathVariable Long id, Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return postService.getPost(id, userId);
    }

    // 发帖（需登录，支持普通/渔获/测评类型）
    @PostMapping("/posts")
    public Result<?> createPost(@Valid @RequestBody PostCreateRequest request, Authentication auth) {
        return postService.createPostWithExtensions(request, (Long) auth.getPrincipal());
    }

    // 更新帖子
    @PutMapping("/posts/{id}")
    public Result<?> updatePost(@PathVariable Long id, @RequestBody Post post, Authentication auth) {
        return postService.updatePost(id, post, (Long) auth.getPrincipal());
    }

    // 删除帖子
    @DeleteMapping("/posts/{id}")
    public Result<?> deletePost(@PathVariable Long id, Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return postService.deletePost(id, (Long) auth.getPrincipal(), role);
    }

    // 获取用户的帖子
    @GetMapping("/users/{userId}/posts")
    public Result<?> getUserPosts(@PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return postService.getUserPosts(userId, page, size);
    }

    // 获取所有板块（公开）
    @GetMapping("/sections")
    public Result<?> listSections() {
        return postService.listSections();
    }

    // 获取板块下标签（公开）
    @GetMapping("/tags")
    public Result<?> listTags(@RequestParam(required = false) Long sectionId) {
        return postService.listTags(sectionId);
    }

    // 热门帖子
    @GetMapping("/posts/hot")
    public Result<?> getHotPosts(@RequestParam(defaultValue = "5") int limit) {
        return postService.getHotPosts(limit);
    }

    // 热门标签
    @GetMapping("/tags/hot")
    public Result<?> getHotTags(@RequestParam(defaultValue = "15") int limit) {
        return postService.getHotTags(limit);
    }
}
