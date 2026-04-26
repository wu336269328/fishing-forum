package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.Result;
import com.fishforum.entity.FishingSpot;
import com.fishforum.entity.SpotReview;
import com.fishforum.entity.User;
import com.fishforum.mapper.FishingSpotMapper;
import com.fishforum.mapper.SpotReviewMapper;
import com.fishforum.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotServiceTest {

    @Mock FishingSpotMapper spotMapper;
    @Mock SpotReviewMapper reviewMapper;
    @Mock UserMapper userMapper;
    @InjectMocks SpotService spotService;

    @Test
    void listSpotsPaginatesAndEnrichesCreator() {
        FishingSpot spot = spot(7L, 3L);
        Page<FishingSpot> page = new Page<>(1, 10);
        page.setRecords(List.of(spot));
        page.setTotal(1);
        when(spotMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        User user = new User(); user.setId(3L); user.setUsername("creator");
        when(userMapper.selectById(3L)).thenReturn(user);

        Result<?> result = spotService.listSpots("鲫鱼", "水库", 1, 10);

        FishingSpot returned = (FishingSpot) ((List<?>) ((Map<?, ?>) result.getData()).get("records")).get(0);
        assertThat(returned.getAuthorName()).isEqualTo("creator");
    }

    @Test
    void createSpotInitializesOwnerRatingAndReviewCount() {
        FishingSpot spot = new FishingSpot();
        spot.setName("新钓点");

        Result<?> result = spotService.createSpot(spot, 3L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(spot.getUserId()).isEqualTo(3L);
        assertThat(spot.getRating()).isEqualTo(0.0);
        assertThat(spot.getReviewCount()).isZero();
        verify(spotMapper).insert(spot);
    }

    @Test
    void updateAndDeleteRejectNonOwnerUnlessAdmin() {
        FishingSpot spot = spot(7L, 3L);
        when(spotMapper.selectById(7L)).thenReturn(spot);

        assertThat(spotService.updateSpot(7L, new FishingSpot(), 4L).getCode()).isEqualTo(403);
        assertThat(spotService.deleteSpot(7L, 4L, "USER").getCode()).isEqualTo(403);
        assertThat(spotService.deleteSpot(7L, 4L, "ADMIN").getCode()).isEqualTo(200);
        verify(spotMapper).deleteById(7L);
    }

    @Test
    void addReviewRecalculatesAverageRating() {
        FishingSpot spot = spot(7L, 3L);
        SpotReview one = review(5);
        SpotReview two = review(4);
        when(reviewMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(one, two));
        when(spotMapper.selectById(7L)).thenReturn(spot);

        Result<?> result = spotService.addReview(7L, 5, "不错", 4L);

        assertThat(result.getData()).isEqualTo("评价成功");
        assertThat(spot.getRating()).isEqualTo(4.5);
        assertThat(spot.getReviewCount()).isEqualTo(2);
        verify(reviewMapper).insert(any(SpotReview.class));
        verify(spotMapper).updateById(spot);
    }

    private static FishingSpot spot(Long id, Long userId) {
        FishingSpot spot = new FishingSpot();
        spot.setId(id);
        spot.setUserId(userId);
        spot.setName("水库");
        spot.setRating(0.0);
        spot.setReviewCount(0);
        return spot;
    }

    private static SpotReview review(int rating) {
        SpotReview review = new SpotReview();
        review.setRating(rating);
        return review;
    }
}
