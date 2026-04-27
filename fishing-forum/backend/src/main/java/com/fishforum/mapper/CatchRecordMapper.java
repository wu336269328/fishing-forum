package com.fishforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fishforum.entity.CatchRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CatchRecordMapper extends BaseMapper<CatchRecord> {
    @Delete("DELETE FROM catch_records WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);
}
