package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 钓点评价实体
 */
@Data
@TableName("spot_reviews")
public class SpotReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spotId;
    private Long userId;
    private Integer rating; // 1-5
    private String content;
    private LocalDateTime createdAt;

    // 非数据库字段
    @TableField(exist = false)
    private String authorName;
    @TableField(exist = false)
    private String authorAvatar;
}
