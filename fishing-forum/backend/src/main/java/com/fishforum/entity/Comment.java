package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论实体（树形结构）
 */
@Data
@TableName("comments")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String content;
    private Long postId;
    private Long userId;
    private Long parentId; // 父评论ID，null表示顶级评论
    private Integer likeCount;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;

    // 非数据库字段
    @TableField(exist = false)
    private String authorName;
    @TableField(exist = false)
    private String authorAvatar;
    @TableField(exist = false)
    private List<Comment> children; // 子评论列表
    @TableField(exist = false)
    private Boolean liked;
}
