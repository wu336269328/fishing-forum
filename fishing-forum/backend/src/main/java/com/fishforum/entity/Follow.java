package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 关注实体
 */
@Data
@TableName("follows")
public class Follow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long followerId; // 关注者
    private Long followingId; // 被关注者
    private LocalDateTime createdAt;
}
