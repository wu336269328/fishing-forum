package com.fishforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fishforum.entity.Like;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper extends BaseMapper<Like> {
    @Delete("DELETE FROM likes WHERE target_type = #{targetType} AND target_id = #{targetId}")
    int deleteByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);
}
