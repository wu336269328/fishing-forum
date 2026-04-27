package com.fishforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fishforum.entity.Post;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    @Update("UPDATE posts SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    @Update("UPDATE posts SET like_count = GREATEST(like_count + #{delta}, 0) WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id, @Param("delta") int delta);

    @Update("UPDATE posts SET comment_count = GREATEST(comment_count + #{delta}, 0) WHERE id = #{id}")
    int incrementCommentCount(@Param("id") Long id, @Param("delta") int delta);

    @Delete("DELETE FROM posts WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
