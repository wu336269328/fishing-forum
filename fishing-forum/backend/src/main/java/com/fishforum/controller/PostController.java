package com.fishforum.controller;

import com.fishforum.common.Result;
import com.fishforum.entity.*;
import com.fishforum.service.AdminService;
import com.fishforum.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

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
    public Result<?> createPost(@RequestBody Map<String, Object> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Post post = new Post();
        post.setTitle((String) body.get("title"));
        post.setContent((String) body.get("content"));
        post.setSectionId(body.get("sectionId") != null ? Long.valueOf(body.get("sectionId").toString()) : null);
        post.setTagId(body.get("tagId") != null ? Long.valueOf(body.get("tagId").toString()) : null);
        post.setPostType(body.get("postType") != null ? (String) body.get("postType") : "NORMAL");

        Result<?> result = postService.createPost(post, userId);
        if (result.getCode() != 200)
            return result;

        // 保存渔获/测评元数据
        if ("CATCH".equals(post.getPostType()) && body.get("catchRecord") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cr = (Map<String, Object>) body.get("catchRecord");
            CatchRecord record = new CatchRecord();
            record.setPostId(post.getId());
            record.setFishSpecies((String) cr.get("fishSpecies"));
            record.setWeight(cr.get("weight") != null ? Double.valueOf(cr.get("weight").toString()) : null);
            record.setLength(cr.get("length") != null ? Double.valueOf(cr.get("length").toString()) : null);
            record.setBait((String) cr.get("bait"));
            record.setSpotName((String) cr.get("spotName"));
            record.setWeather((String) cr.get("weather"));
            record.setPhotoUrl((String) cr.get("photoUrl"));
            if (cr.get("fishingDate") != null)
                record.setFishingDate(LocalDate.parse(cr.get("fishingDate").toString()));
            postService.saveCatchRecord(record);
        } else if ("REVIEW".equals(post.getPostType()) && body.get("gearReview") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> gr = (Map<String, Object>) body.get("gearReview");
            GearReview review = new GearReview();
            review.setPostId(post.getId());
            review.setBrand((String) gr.get("brand"));
            review.setModel((String) gr.get("model"));
            review.setGearCategory((String) gr.get("gearCategory"));
            review.setPrice(gr.get("price") != null ? Double.valueOf(gr.get("price").toString()) : null);
            review.setRating(gr.get("rating") != null ? Integer.valueOf(gr.get("rating").toString()) : null);
            review.setPros((String) gr.get("pros"));
            review.setCons((String) gr.get("cons"));
            review.setPhotoUrl((String) gr.get("photoUrl"));
            postService.saveGearReview(review);
        }
        return result;
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
