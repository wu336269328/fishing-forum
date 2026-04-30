package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.PageUtils;
import com.fishforum.common.Result;
import com.fishforum.dto.SpotCreateRequest;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 钓点服务 - 钓点CRUD、评分、评价
 */
@Service
@RequiredArgsConstructor
public class SpotService {

    private final FishingSpotMapper spotMapper;
    private final SpotReviewMapper reviewMapper;
    private final SpotFavoriteMapper spotFavoriteMapper;
    private final UserMapper userMapper;

    private static final Set<String> SPOT_TYPES = Set.of("水库", "河流", "湖泊", "海钓", "黑坑");

    // 获取钓点列表
    public Result<?> listSpots(String keyword, String spotType, int page, int size) {
        Page<FishingSpot> pageObj = PageUtils.pageRequest(page, size);
        LambdaQueryWrapper<FishingSpot> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(FishingSpot::getName, keyword).or().like(FishingSpot::getFishTypes, keyword);
        }
        if (spotType != null && !spotType.isEmpty()) {
            wrapper.eq(FishingSpot::getSpotType, spotType);
        }
        wrapper.orderByDesc(FishingSpot::getRating);
        Page<FishingSpot> result = spotMapper.selectPage(pageObj, wrapper);
        fillSpotAuthors(result.getRecords());

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
    @Transactional
    public Result<?> createSpot(SpotCreateRequest request, Long userId) {
        Result<?> validation = validateSpotRequest(request);
        if (validation.getCode() != 200) {
            return validation;
        }
        FishingSpot spot = toSpot(request);
        spot.setUserId(userId);
        spot.setRating(0.0);
        spot.setReviewCount(0);
        spotMapper.insert(spot);
        return Result.ok("钓点添加成功", spot);
    }

    @Transactional
    public Result<?> createSpot(FishingSpot spot, Long userId) {
        SpotCreateRequest request = toRequest(spot);
        Result<?> validation = validateSpotRequest(request);
        if (validation.getCode() != 200) {
            return validation;
        }
        applySpotRequest(spot, request);
        spot.setUserId(userId);
        spot.setRating(0.0);
        spot.setReviewCount(0);
        spotMapper.insert(spot);
        return Result.ok("钓点添加成功", spot);
    }

    @Transactional
    public Result<?> updateSpot(Long id, FishingSpot update, Long userId, String role) {
        FishingSpot spot = spotMapper.selectById(id);
        if (spot == null)
            return Result.error(404, "钓点不存在");
        if (!spot.getUserId().equals(userId) && !"ADMIN".equals(role))
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
        if (update.getBestSeason() != null)
            spot.setBestSeason(update.getBestSeason());
        if (update.getFeeInfo() != null)
            spot.setFeeInfo(update.getFeeInfo());
        if (update.getNoFishingNotice() != null)
            spot.setNoFishingNotice(update.getNoFishingNotice());
        if (update.getLatitude() != null)
            spot.setLatitude(update.getLatitude());
        if (update.getLongitude() != null)
            spot.setLongitude(update.getLongitude());
        if (update.getImageUrl() != null)
            spot.setImageUrl(update.getImageUrl());
        spotMapper.updateById(spot);
        return Result.ok("更新成功");
    }

    // 更新钓点
    @Transactional
    public Result<?> updateSpot(Long id, SpotCreateRequest request, Long userId, String role) {
        FishingSpot spot = spotMapper.selectById(id);
        if (spot == null)
            return Result.error(404, "钓点不存在");
        if (!spot.getUserId().equals(userId) && !"ADMIN".equals(role))
            return Result.error(403, "无权修改");
        Result<?> validation = validateSpotRequest(request);
        if (validation.getCode() != 200) {
            return validation;
        }
        applySpotRequest(spot, request);
        spotMapper.updateById(spot);
        return Result.ok("更新成功");
    }

    // 删除钓点
    @Transactional
    public Result<?> deleteSpot(Long id, Long userId, String role) {
        FishingSpot spot = spotMapper.selectById(id);
        if (spot == null)
            return Result.error(404, "钓点不存在");
        if (!spot.getUserId().equals(userId) && !"ADMIN".equals(role))
            return Result.error(403, "无权删除");
        spotFavoriteMapper.deleteBySpotId(id);
        spotMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    // 添加评价
    @Transactional
    public Result<?> addReview(Long spotId, Integer rating, String content, Long userId) {
        FishingSpot spot = spotMapper.selectById(spotId);
        if (spot == null) {
            return Result.error(404, "钓点不存在");
        }
        if (rating == null || rating < 1 || rating > 5) {
            return Result.error(400, "评分必须在1到5之间");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "评价内容不能为空");
        }
        if (content.length() > 1000) {
            return Result.error(400, "评价内容不能超过1000字");
        }
        SpotReview review = new SpotReview();
        review.setSpotId(spotId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setContent(content.trim());
        reviewMapper.insert(review);
        // 更新平均评分
        updateRating(spotId);
        return Result.ok("评价成功");
    }

    @Transactional
    public Result<?> toggleFavorite(Long spotId, Long userId) {
        FishingSpot spot = spotMapper.selectById(spotId);
        if (spot == null) {
            return Result.error(404, "钓点不存在");
        }
        LambdaQueryWrapper<SpotFavorite> wrapper = new LambdaQueryWrapper<SpotFavorite>()
                .eq(SpotFavorite::getUserId, userId)
                .eq(SpotFavorite::getSpotId, spotId);
        SpotFavorite existing = spotFavoriteMapper.selectOne(wrapper);
        if (existing != null) {
            spotFavoriteMapper.deleteById(existing.getId());
            return Result.ok("已取消收藏");
        }
        SpotFavorite favorite = new SpotFavorite();
        favorite.setSpotId(spotId);
        favorite.setUserId(userId);
        spotFavoriteMapper.insert(favorite);
        return Result.ok("已收藏钓点");
    }

    public Result<?> getUserFavorites(Long userId) {
        List<SpotFavorite> favorites = spotFavoriteMapper.selectList(
                new LambdaQueryWrapper<SpotFavorite>()
                        .eq(SpotFavorite::getUserId, userId)
                        .orderByDesc(SpotFavorite::getCreatedAt));
        List<FishingSpot> spots = favorites.stream()
                .map(f -> spotMapper.selectById(f.getSpotId()))
                .filter(s -> s != null)
                .toList();
        fillSpotAuthors(spots);
        return Result.ok(spots);
    }

    // 获取钓点评价列表
    public Result<?> getReviews(Long spotId) {
        List<SpotReview> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<SpotReview>().eq(SpotReview::getSpotId, spotId)
                        .orderByDesc(SpotReview::getCreatedAt));
        fillReviewAuthors(reviews);
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

    private SpotCreateRequest toRequest(FishingSpot spot) {
        SpotCreateRequest request = new SpotCreateRequest();
        request.setName(spot.getName() != null ? spot.getName() : "未命名钓点");
        request.setDescription(spot.getDescription());
        request.setLatitude(spot.getLatitude() != null ? spot.getLatitude() : 0.0);
        request.setLongitude(spot.getLongitude() != null ? spot.getLongitude() : 0.0);
        request.setFishTypes(spot.getFishTypes());
        request.setSpotType(spot.getSpotType() != null ? spot.getSpotType() : "水库");
        request.setOpenTime(spot.getOpenTime());
        request.setBestSeason(spot.getBestSeason());
        request.setFeeInfo(spot.getFeeInfo());
        request.setNoFishingNotice(spot.getNoFishingNotice());
        request.setImageUrl(spot.getImageUrl());
        return request;
    }

    private Result<?> validateSpotRequest(SpotCreateRequest request) {
        if (!SPOT_TYPES.contains(request.getSpotType())) {
            return Result.error(400, "钓点类型不合法");
        }
        return Result.ok();
    }

    private FishingSpot toSpot(SpotCreateRequest request) {
        FishingSpot spot = new FishingSpot();
        applySpotRequest(spot, request);
        return spot;
    }

    private void applySpotRequest(FishingSpot spot, SpotCreateRequest request) {
        spot.setName(request.getName().trim());
        spot.setDescription(request.getDescription());
        spot.setLatitude(request.getLatitude());
        spot.setLongitude(request.getLongitude());
        spot.setFishTypes(request.getFishTypes());
        spot.setSpotType(request.getSpotType());
        spot.setOpenTime(request.getOpenTime());
        spot.setBestSeason(request.getBestSeason());
        spot.setFeeInfo(request.getFeeInfo());
        spot.setNoFishingNotice(request.getNoFishingNotice());
        spot.setImageUrl(request.getImageUrl());
    }

    private void fillSpotAuthors(List<FishingSpot> spots) {
        Map<Long, User> users = usersById(spots.stream().map(FishingSpot::getUserId).collect(Collectors.toSet()));
        spots.forEach(spot -> {
            User user = users.get(spot.getUserId());
            if (user != null) {
                spot.setAuthorName(user.getUsername());
            }
        });
    }

    private void fillReviewAuthors(List<SpotReview> reviews) {
        Map<Long, User> users = usersById(reviews.stream().map(SpotReview::getUserId).collect(Collectors.toSet()));
        reviews.forEach(review -> {
            User user = users.get(review.getUserId());
            if (user != null) {
                review.setAuthorName(user.getUsername());
                review.setAuthorAvatar(user.getAvatar());
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

    private void enrichSpot(FishingSpot spot) {
        User u = userMapper.selectById(spot.getUserId());
        if (u != null)
            spot.setAuthorName(u.getUsername());
    }
}
