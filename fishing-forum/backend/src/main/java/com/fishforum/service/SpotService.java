package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.Result;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 钓点服务 - 钓点CRUD、评分、评价
 */
@Service
@RequiredArgsConstructor
public class SpotService {

    private final FishingSpotMapper spotMapper;
    private final SpotReviewMapper reviewMapper;
    private final UserMapper userMapper;

    // 获取钓点列表
    public Result<?> listSpots(String keyword, String spotType, int page, int size) {
        Page<FishingSpot> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<FishingSpot> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(FishingSpot::getName, keyword).or().like(FishingSpot::getFishTypes, keyword);
        }
        if (spotType != null && !spotType.isEmpty()) {
            wrapper.eq(FishingSpot::getSpotType, spotType);
        }
        wrapper.orderByDesc(FishingSpot::getRating);
        Page<FishingSpot> result = spotMapper.selectPage(pageObj, wrapper);
        result.getRecords().forEach(this::enrichSpot);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return Result.ok(data);
    }

    // 获取所有钓点（地图展示用）
    public Result<?> getAllSpots() {
        List<FishingSpot> spots = spotMapper.selectList(new LambdaQueryWrapper<>());
        return Result.ok(spots);
    }

    // 获取钓点详情
    public Result<?> getSpot(Long id) {
        FishingSpot spot = spotMapper.selectById(id);
        if (spot == null)
            return Result.error(404, "钓点不存在");
        enrichSpot(spot);
        return Result.ok(spot);
    }

    // 新增钓点
    public Result<?> createSpot(FishingSpot spot, Long userId) {
        spot.setUserId(userId);
        spot.setRating(0.0);
        spot.setReviewCount(0);
        spotMapper.insert(spot);
        return Result.ok("钓点添加成功", spot);
    }

    // 更新钓点
    public Result<?> updateSpot(Long id, FishingSpot update, Long userId) {
        FishingSpot spot = spotMapper.selectById(id);
        if (spot == null)
            return Result.error(404, "钓点不存在");
        if (!spot.getUserId().equals(userId))
            return Result.error(403, "无权修改");
        if (update.getName() != null)
            spot.setName(update.getName());
        if (update.getDescription() != null)
            spot.setDescription(update.getDescription());
        if (update.getFishTypes() != null)
            spot.setFishTypes(update.getFishTypes());
        if (update.getSpotType() != null)
            spot.setSpotType(update.getSpotType());
        if (update.getOpenTime() != null)
            spot.setOpenTime(update.getOpenTime());
        spotMapper.updateById(spot);
        return Result.ok("更新成功");
    }

    // 删除钓点
    public Result<?> deleteSpot(Long id, Long userId, String role) {
        FishingSpot spot = spotMapper.selectById(id);
        if (spot == null)
            return Result.error(404, "钓点不存在");
        if (!spot.getUserId().equals(userId) && !"ADMIN".equals(role))
            return Result.error(403, "无权删除");
        spotMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    // 添加评价
    public Result<?> addReview(Long spotId, Integer rating, String content, Long userId) {
        SpotReview review = new SpotReview();
        review.setSpotId(spotId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setContent(content);
        reviewMapper.insert(review);
        // 更新平均评分
        updateRating(spotId);
        return Result.ok("评价成功");
    }

    // 获取钓点评价列表
    public Result<?> getReviews(Long spotId) {
        List<SpotReview> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<SpotReview>().eq(SpotReview::getSpotId, spotId)
                        .orderByDesc(SpotReview::getCreatedAt));
        reviews.forEach(r -> {
            User u = userMapper.selectById(r.getUserId());
            if (u != null) {
                r.setAuthorName(u.getUsername());
                r.setAuthorAvatar(u.getAvatar());
            }
        });
        return Result.ok(reviews);
    }

    // 更新平均评分
    private void updateRating(Long spotId) {
        List<SpotReview> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<SpotReview>().eq(SpotReview::getSpotId, spotId));
        if (!reviews.isEmpty()) {
            double avg = reviews.stream().mapToInt(SpotReview::getRating).average().orElse(0);
            FishingSpot spot = spotMapper.selectById(spotId);
            spot.setRating(Math.round(avg * 10) / 10.0);
            spot.setReviewCount(reviews.size());
            spotMapper.updateById(spot);
        }
    }

    private void enrichSpot(FishingSpot spot) {
        User u = userMapper.selectById(spot.getUserId());
        if (u != null)
            spot.setAuthorName(u.getUsername());
    }
}
