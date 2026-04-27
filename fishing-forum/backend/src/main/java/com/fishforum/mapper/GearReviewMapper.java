package com.fishforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fishforum.entity.GearReview;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GearReviewMapper extends BaseMapper<GearReview> {
    @Delete("DELETE FROM gear_reviews WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);
}
