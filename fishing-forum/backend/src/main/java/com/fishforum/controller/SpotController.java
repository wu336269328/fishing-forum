package com.fishforum.controller;

import com.fishforum.common.Result;
import com.fishforum.entity.FishingSpot;
import com.fishforum.service.SpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 钓点控制器
 */
@RestController
@RequestMapping("/api/spots")
@RequiredArgsConstructor
public class SpotController {

    private final SpotService spotService;

    // 获取钓点列表（公开）
    @GetMapping
    public Result<?> listSpots(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String spotType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return spotService.listSpots(keyword, spotType, page, size);
    }

    // 获取所有钓点（地图用，公开）
    @GetMapping("/all")
    public Result<?> getAllSpots() {
        return spotService.getAllSpots();
    }

    // 获取钓点详情（公开）
    @GetMapping("/{id}")
    public Result<?> getSpot(@PathVariable Long id) {
        return spotService.getSpot(id);
    }

    // 新增钓点
    @PostMapping
    public Result<?> createSpot(@RequestBody FishingSpot spot, Authentication auth) {
        return spotService.createSpot(spot, (Long) auth.getPrincipal());
    }

    // 更新钓点
    @PutMapping("/{id}")
    public Result<?> updateSpot(@PathVariable Long id, @RequestBody FishingSpot spot, Authentication auth) {
        return spotService.updateSpot(id, spot, (Long) auth.getPrincipal());
    }

    // 删除钓点
    @DeleteMapping("/{id}")
    public Result<?> deleteSpot(@PathVariable Long id, Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return spotService.deleteSpot(id, (Long) auth.getPrincipal(), role);
    }

    // 添加评价
    @PostMapping("/{spotId}/reviews")
    public Result<?> addReview(@PathVariable Long spotId, @RequestBody Map<String, Object> body, Authentication auth) {
        Integer rating = (Integer) body.get("rating");
        String content = (String) body.get("content");
        return spotService.addReview(spotId, rating, content, (Long) auth.getPrincipal());
    }

    // 获取钓点评价（公开）
    @GetMapping("/{spotId}/reviews")
    public Result<?> getReviews(@PathVariable Long spotId) {
        return spotService.getReviews(spotId);
    }

    // 收藏/取消收藏钓点
    @PostMapping("/{spotId}/favorite")
    public Result<?> toggleFavorite(@PathVariable Long spotId, Authentication auth) {
        return spotService.toggleFavorite(spotId, (Long) auth.getPrincipal());
    }

}
