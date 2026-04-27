package com.fishforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fishforum.entity.Favorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    @Delete("DELETE FROM favorites WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);
}
