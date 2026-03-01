package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 私信实体
 */
@Data
@TableName("messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;

    // 非数据库字段
    @TableField(exist = false)
    private String senderName;
    @TableField(exist = false)
    private String senderAvatar;
    @TableField(exist = false)
    private String receiverName;
}
