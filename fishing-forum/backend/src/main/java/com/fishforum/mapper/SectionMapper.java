package com.fishforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fishforum.entity.Section;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SectionMapper extends BaseMapper<Section> {
    @Update("UPDATE sections SET post_count = GREATEST(post_count + #{delta}, 0) WHERE id = #{id}")
    int incrementPostCount(@Param("id") Long id, @Param("delta") int delta);
}
