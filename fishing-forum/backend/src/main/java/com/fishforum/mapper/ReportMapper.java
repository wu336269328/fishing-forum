package com.fishforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fishforum.entity.Report;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {
    @Delete("DELETE FROM reports WHERE target_type = #{targetType} AND target_id = #{targetId}")
    int deleteByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);
}
