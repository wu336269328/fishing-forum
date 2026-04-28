package com.fishforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fishforum.entity.SpotFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SpotFavoriteMapper extends BaseMapper<SpotFavorite> {
    @Delete("DELETE FROM spot_favorites WHERE spot_id = #{spotId}")
    int deleteBySpotId(@Param("spotId") Long spotId);
}
