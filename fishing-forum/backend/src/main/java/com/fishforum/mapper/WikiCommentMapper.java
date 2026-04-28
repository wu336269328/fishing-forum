package com.fishforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fishforum.entity.WikiComment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WikiCommentMapper extends BaseMapper<WikiComment> {
    @Delete("DELETE FROM wiki_comments WHERE entry_id = #{entryId}")
    int deleteByEntryId(@Param("entryId") Long entryId);

    @Update("UPDATE wiki_comments SET like_count = GREATEST(like_count + #{delta}, 0) WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id, @Param("delta") int delta);
}
