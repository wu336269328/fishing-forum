package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 帖子实体
 */
@Data
@TableName("posts")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private Long sectionId;
    private Long tagId;
    private String postType; // NORMAL / CATCH / REVIEW
    private Boolean isTop;
    private Boolean isFeatured;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    // 非数据库字段
    @TableField(exist = false)
    private String authorName;
    @TableField(exist = false)
    private String authorAvatar;
    @TableField(exist = false)
    private String sectionName;
    @TableField(exist = false)
    private Boolean liked;
    @TableField(exist = false)
    private Boolean favorited;
    @TableField(exist = false)
    private String tagName;
}
