package com.fishforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fishforum.entity.Comment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    @Delete("DELETE FROM comments WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);

    @Update("UPDATE comments SET like_count = GREATEST(like_count + #{delta}, 0) WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id, @Param("delta") int delta);
}
