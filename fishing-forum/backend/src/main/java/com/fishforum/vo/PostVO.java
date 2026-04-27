package com.fishforum.vo;

import com.fishforum.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostVO {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private Long sectionId;
    private Long tagId;
    private String postType;
    private Boolean isTop;
    private Boolean isFeatured;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorName;
    private String authorAvatar;
    private String sectionName;
    private Boolean liked;
    private Boolean favorited;
    private String tagName;

    public static PostVO from(Post post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setUserId(post.getUserId());
        vo.setSectionId(post.getSectionId());
        vo.setTagId(post.getTagId());
        vo.setPostType(post.getPostType());
        vo.setIsTop(post.getIsTop());
        vo.setIsFeatured(post.getIsFeatured());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        vo.setAuthorName(post.getAuthorName());
        vo.setAuthorAvatar(post.getAuthorAvatar());
        vo.setSectionName(post.getSectionName());
        vo.setLiked(post.getLiked());
        vo.setFavorited(post.getFavorited());
        vo.setTagName(post.getTagName());
        return vo;
    }
}
