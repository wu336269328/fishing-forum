package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
@TableName("notifications")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type; // LIKE / COMMENT / FOLLOW / SYSTEM
    private String title;
    private String content;
    private Long relatedId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
